use sqlx::PgPool;
use uuid::Uuid;
use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use crate::error::AppError;

#[derive(Debug, Serialize, Deserialize, Clone, sqlx::FromRow)]
pub struct List {
    pub id: Uuid,
    pub user_id: String,
    pub name: String,
    pub description: Option<String>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

#[derive(Debug, Serialize, Deserialize, Clone, sqlx::FromRow)]
pub struct ListItem {
    pub id: Uuid,
    pub list_id: Uuid,
    pub item_type: String,
    pub item_id: String,
    pub position: i32,
    pub notes: Option<String>,
    pub added_at: DateTime<Utc>,
}

#[derive(Clone)]
pub struct DbService {
    pool: PgPool,
}

impl DbService {
    pub async fn new(database_url: &str) -> Result<Self, AppError> {
        let pool = PgPool::connect(database_url)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!("DB connect error: {}", e)))?;

        sqlx::migrate!("./migrations")
            .run(&pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!("Migration error: {}", e)))?;

        Ok(DbService { pool })
    }

    pub fn disabled() -> Self {
        let pool = PgPool::connect_lazy("postgresql://disabled:disabled@localhost/disabled")
            .expect("lazy pool creation failed");
        DbService { pool }
    }

    // ── Lists ─────────────────────────────────────────────────────────────────

    pub async fn create_list(&self, user_id: &str, name: &str, description: Option<&str>) -> Result<List, AppError> {
        sqlx::query_as(
            "INSERT INTO lists (user_id, name, description)
             VALUES ($1, $2, $3)
             RETURNING id, user_id, name, description, created_at, updated_at",
        )
            .bind(user_id).bind(name).bind(description)
            .fetch_one(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))
    }

    pub async fn get_list(&self, list_id: Uuid, user_id: &str) -> Result<Option<List>, AppError> {
        sqlx::query_as(
            "SELECT id, user_id, name, description, created_at, updated_at
             FROM lists WHERE id = $1 AND user_id = $2",
        )
            .bind(list_id).bind(user_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))
    }

    pub async fn get_lists_for_user(&self, user_id: &str) -> Result<Vec<List>, AppError> {
        sqlx::query_as(
            "SELECT id, user_id, name, description, created_at, updated_at
             FROM lists WHERE user_id = $1 ORDER BY created_at DESC",
        )
            .bind(user_id)
            .fetch_all(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))
    }

    pub async fn update_list(&self, list_id: Uuid, user_id: &str, name: Option<&str>, description: Option<&str>) -> Result<Option<List>, AppError> {
        sqlx::query_as(
            "UPDATE lists
             SET name        = COALESCE($3, name),
                 description = COALESCE($4, description),
                 updated_at  = now()
             WHERE id = $1 AND user_id = $2
             RETURNING id, user_id, name, description, created_at, updated_at",
        )
            .bind(list_id).bind(user_id).bind(name).bind(description)
            .fetch_optional(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))
    }

    pub async fn delete_list(&self, list_id: Uuid, user_id: &str) -> Result<bool, AppError> {
        let result = sqlx::query("DELETE FROM lists WHERE id = $1 AND user_id = $2")
            .bind(list_id).bind(user_id)
            .execute(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))?;
        Ok(result.rows_affected() > 0)
    }

    // ── List items ────────────────────────────────────────────────────────────

    pub async fn add_item(&self, list_id: Uuid, item_type: &str, item_id: &str, notes: Option<&str>) -> Result<ListItem, AppError> {
        let pos: i32 = sqlx::query_scalar(
            "SELECT COALESCE(MAX(position), -1) FROM list_items WHERE list_id = $1",
        )
            .bind(list_id)
            .fetch_one(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))?;

        sqlx::query_as(
            "INSERT INTO list_items (list_id, item_type, item_id, position, notes)
             VALUES ($1, $2, $3, $4, $5)
             ON CONFLICT (list_id, item_id, item_type) DO NOTHING
             RETURNING id, list_id, item_type, item_id, position, notes, added_at",
        )
            .bind(list_id).bind(item_type).bind(item_id).bind(pos + 1).bind(notes)
            .fetch_one(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))
    }

    pub async fn get_items(&self, list_id: Uuid) -> Result<Vec<ListItem>, AppError> {
        sqlx::query_as(
            "SELECT id, list_id, item_type, item_id, position, notes, added_at
             FROM list_items WHERE list_id = $1 ORDER BY position ASC",
        )
            .bind(list_id)
            .fetch_all(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))
    }

    pub async fn remove_item(&self, list_id: Uuid, item_id: &str) -> Result<bool, AppError> {
        let result = sqlx::query("DELETE FROM list_items WHERE list_id = $1 AND item_id = $2")
            .bind(list_id).bind(item_id)
            .execute(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))?;
        Ok(result.rows_affected() > 0)
    }

    pub async fn update_item_notes(&self, list_id: Uuid, item_id: &str, notes: Option<&str>) -> Result<Option<ListItem>, AppError> {
        sqlx::query_as(
            "UPDATE list_items SET notes = $3
             WHERE list_id = $1 AND item_id = $2
             RETURNING id, list_id, item_type, item_id, position, notes, added_at",
        )
            .bind(list_id).bind(item_id).bind(notes)
            .fetch_optional(&self.pool)
            .await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))
    }

    /// Reorder items by providing item_ids in the new desired order.
    /// Position 0 = first. Items not in the list are ignored.
    pub async fn reorder_items(&self, list_id: Uuid, item_ids: &[String]) -> Result<Vec<ListItem>, AppError> {
        let mut tx = self.pool.begin().await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))?;

        for (position, item_id) in item_ids.iter().enumerate() {
            sqlx::query(
                "UPDATE list_items SET position = $3 WHERE list_id = $1 AND item_id = $2",
            )
                .bind(list_id).bind(item_id).bind(position as i32)
                .execute(&mut *tx)
                .await
                .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))?;
        }

        tx.commit().await
            .map_err(|e| AppError::Internal(anyhow::anyhow!(e)))?;

        self.get_items(list_id).await
    }
}