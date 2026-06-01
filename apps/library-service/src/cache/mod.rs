use redis::{aio::ConnectionManager, AsyncCommands, Client};
use serde::de::DeserializeOwned;
use serde::Serialize;
use tracing::{debug, warn};

use crate::dto::request::SearchType;
use crate::error::AppError;

/// 30 days in seconds — media metadata barely ever changes.
pub const CACHE_TTL_SECONDS: usize = 30 * 24 * 60 * 60; // 2_592_000

/// Wraps a Redis `ConnectionManager` (auto-reconnects on drop).
///
/// All cache operations are fire-and-forget on write — a Redis
/// failure never surfaces as a user-visible error. On read, a
/// cache miss is treated the same as a Redis error (just fetch
/// from the API instead).
#[derive(Clone)]
pub struct CacheService {
    conn: ConnectionManager,
}

impl CacheService {
    /// Connect to Redis. Call once at startup.
    ///
    /// `redis_url` example: `redis://127.0.0.1:6379`
    pub async fn new(redis_url: &str) -> Result<Self, AppError> {
        let client = Client::open(redis_url)
            .map_err(|e| AppError::Cache(format!("Redis connect error: {}", e)))?;

        let conn = ConnectionManager::new(client)
            .await
            .map_err(|e| AppError::Cache(format!("Redis connection manager error: {}", e)))?;

        tracing::info!("Redis cache connected: {}", redis_url);
        Ok(CacheService { conn })
    }

    // ─── Public API ─────────────────────────────────────────────────────────

    /// Try to get a cached value. Returns `None` on miss or any Redis error.
    /// A Redis hiccup should never break the user's request.
    pub async fn get<T: DeserializeOwned>(&self, key: &str) -> Option<T> {
        let mut conn = self.conn.clone();
        match conn.get::<_, Option<String>>(key).await {
            Ok(Some(json)) => match serde_json::from_str::<T>(&json) {
                Ok(val) => {
                    debug!("Cache HIT: {}", key);
                    Some(val)
                }
                Err(e) => {
                    warn!("Cache deserialize error for key {}: {}", key, e);
                    None
                }
            },
            Ok(None) => {
                debug!("Cache MISS: {}", key);
                None
            }
            Err(e) => {
                warn!("Redis GET error for key {}: {}", key, e);
                None
            }
        }
    }

    /// Store a value in Redis with the configured TTL (30 days).
    /// Errors are logged but not propagated — a write failure is non-fatal.
    pub async fn set<T: Serialize>(&self, key: &str, value: &T) {
        let mut conn = self.conn.clone();
        match serde_json::to_string(value) {
            Ok(json) => {
                let result: redis::RedisResult<()> =
                    conn.set_ex(key, json, CACHE_TTL_SECONDS).await;
                match result {
                    Ok(_) => debug!("Cache SET: {} (TTL={}s)", key, CACHE_TTL_SECONDS),
                    Err(e) => warn!("Redis SET error for key {}: {}", key, e),
                }
            }
            Err(e) => warn!("Cache serialize error for key {}: {}", key, e),
        }
    }

    /// Manually invalidate (delete) a key. Used for admin/testing.
    pub async fn invalidate(&self, key: &str) {
        let mut conn = self.conn.clone();
        let _: redis::RedisResult<()> = conn.del(key).await;
        debug!("Cache INVALIDATED: {}", key);
    }

    // ─── Key builders ────────────────────────────────────────────────────────

    /// Key for a search result list.
    ///
    /// Format: `search:{TYPE}:{query_lowercase}:{max_results}`
    /// Example: `search:BOOK:clean code:10`
    pub fn search_key(search_type: SearchType, query: &str, max_results: usize) -> String {
        format!(
            "search:{}:{}:{}",
            search_type,
            query.to_lowercase().trim(),
            max_results
        )
    }

    /// Key for a single item lookup by ID.
    ///
    /// Format: `item:{TYPE}:{id}`
    /// Example: `item:MOVIE:tt0111161`
    pub fn item_key(search_type: SearchType, id: &str) -> String {
        format!("item:{}:{}", search_type, id)
    }
}
