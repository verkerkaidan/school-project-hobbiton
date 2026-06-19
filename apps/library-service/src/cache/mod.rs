use std::sync::Arc;
use redis::{aio::ConnectionManager, AsyncCommands, Client};
use serde::de::DeserializeOwned;
use serde::Serialize;
use tracing::{debug, warn};
use std::sync::atomic::{AtomicBool, Ordering};

use crate::dto::request::SearchType;
use crate::error::AppError;

pub const CACHE_TTL_SECONDS: usize = 2592000;

#[derive(Clone)]
pub struct CacheService {
    conn: Option<ConnectionManager>,
    cache_enabled: Arc<AtomicBool>,
}

impl CacheService {
    /// Connect to Redis. Call once at startup.
    pub async fn new(redis_url: &str) -> Result<Self, AppError> {
        let client = Client::open(redis_url)
            .map_err(|e| AppError::Cache(format!("Redis connect error: {}", e)))?;

        let conn = ConnectionManager::new(client)
            .await
            .map_err(|e| AppError::Cache(format!("Redis connection manager error: {}", e)))?;

        tracing::info!("Redis cache connected: {}", redis_url);

        Ok(CacheService {
            conn: Some(conn),
            cache_enabled: Arc::new(AtomicBool::new(true)),
        })
    }

    /// A permanently-disabled no-op cache.
    /// Every get returns None, every set is a no-op.
    /// Use this in tests when Redis is unavailable (e.g. Docker build, CI).
    pub fn disabled() -> Self {
        CacheService {
            conn: None,
            cache_enabled: Arc::new(AtomicBool::new(false)),
        }
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    pub async fn get<T: DeserializeOwned>(&self, key: &str) -> Option<T> {
        if !self.is_enabled() {
            return None;
        }

        let mut conn = self.conn.clone()?;

        match conn.get::<_, Option<String>>(key).await {
            Ok(Some(json)) => match serde_json::from_str::<T>(&json) {
                Ok(val) => {
                    debug!("Cache HIT: {}", key);
                    Some(val)
                }
                Err(e) => {
                    warn!("Cache deserialize error for key '{}': {}", key, e);
                    None
                }
            },
            Ok(None) => {
                debug!("Cache MISS: {}", key);
                None
            }
            Err(e) => {
                warn!("Redis GET error for key '{}': {}", key, e);
                self.disable_temporarily();
                None
            }
        }
    }

    pub async fn set<T: Serialize>(&self, key: &str, value: &T) {
        if !self.is_enabled() {
            return;
        }

        let mut conn = match self.conn.clone() {
            Some(c) => c,
            None => return,
        };

        match serde_json::to_string(value) {
            Ok(json) => {
                let result: redis::RedisResult<()> =
                    conn.set_ex(key, json, CACHE_TTL_SECONDS as u64).await;

                if let Err(e) = result {
                    warn!("Redis SET error for key '{}': {}", key, e);
                    self.disable_temporarily();
                }
            }
            Err(e) => {
                warn!("Cache serialize error for key '{}': {}", key, e);
            }
        }
    }

    pub async fn invalidate(&self, key: &str) {
        if !self.is_enabled() {
            return;
        }
        let mut conn = match self.conn.clone() {
            Some(c) => c,
            None => return,
        };
        let _: redis::RedisResult<()> = conn.del(key).await;
        debug!("Cache INVALIDATED: {}", key);
    }

    // ─── Key builders ────────────────────────────────────────────────────────

    pub fn search_key(search_type: SearchType, query: &str, max_results: usize) -> String {
        format!(
            "search:{}:{}:{}",
            search_type,
            query.to_lowercase().trim(),
            max_results
        )
    }

    pub fn item_key(search_type: SearchType, id: &str) -> String {
        format!("item:{}:{}", search_type, id)
    }

    fn is_enabled(&self) -> bool {
        self.cache_enabled.load(Ordering::Relaxed)
    }

    fn disable_temporarily(&self) {
        warn!("Redis unhealthy — disabling cache temporarily");
        self.cache_enabled.store(false, Ordering::Relaxed);
        let flag = self.cache_enabled.clone();
        tokio::spawn(async move {
            tokio::time::sleep(std::time::Duration::from_secs(30)).await;
            flag.store(true, Ordering::Relaxed);
            tracing::info!("Redis cache re-enabled");
        });
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::dto::request::SearchType;

    #[test]
    fn search_key_normalizes_query() {
        let key = CacheService::search_key(SearchType::Book, "  Clean Code  ", 10);
        assert_eq!(key, "search:BOOK:clean code:10");
    }

    #[test]
    fn search_key_handles_movie_type() {
        let key = CacheService::search_key(SearchType::Movie, "Batman", 5);
        assert_eq!(key, "search:MOVIE:batman:5");
    }

    #[test]
    fn item_key_builds_correct_key() {
        let key = CacheService::item_key(SearchType::Movie, "tt0111161");
        assert_eq!(key, "item:MOVIE:tt0111161");
    }

    #[test]
    fn item_key_for_book() {
        let key = CacheService::item_key(SearchType::Book, "9780132350884");
        assert_eq!(key, "item:BOOK:9780132350884");
    }

    #[test]
    fn ttl_is_thirty_days() {
        assert_eq!(CACHE_TTL_SECONDS, 30 * 24 * 60 * 60);
    }
}