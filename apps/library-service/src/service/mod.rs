use std::sync::Arc;

use tracing::{info, warn};

use crate::cache::CacheService;
use crate::dto::request::{SearchRequest, SearchType};
use crate::dto::response::SearchResult;
use crate::error::AppError;
use crate::factory::ServiceFactory;

/// **Service Layer**
///
/// Orchestrates the cache-aside pattern:
///
/// ```text
///  Request
///    │
///    ▼
/// ┌──────────────┐   HIT   ┌───────────────┐
/// │ CacheService │────────▶│ Return cached │
/// └──────────────┘         └───────────────┘
///    │ MISS
///    ▼
/// ┌──────────────┐         ┌───────────────┐
/// │  Strategy    │────────▶│  Store in     │
/// │  (ext. API)  │         │  Redis (30d)  │
/// └──────────────┘         └───────────────┘
///    │
///    ▼
///  Response
/// ```
///
/// Redis failures are **non-fatal** — they only produce a log warning.
/// The service falls through to the live API call transparently.
pub struct LibraryService {
    factory: Arc<ServiceFactory>,
    cache: Arc<CacheService>,
}

impl LibraryService {
    pub fn new(factory: Arc<ServiceFactory>, cache: Arc<CacheService>) -> Self {
        LibraryService { factory, cache }
    }

    // ─── Search (list) ───────────────────────────────────────────────────────

    /// Search flow:
    /// 1. Build cache key from type + query + maxResults
    /// 2. Check Redis — if hit, use cached; if miss, call external API & store list
    /// 3. Always loop through results to seed/refresh individual item caches
    /// 4. Return result
    pub async fn search(
        &self,
        req: &SearchRequest,
    ) -> Result<Vec<SearchResult>, AppError> {
        let cache_key =
            CacheService::search_key(req.search_type, &req.query, req.max_results);

        let results = if let Some(cached) =
            self.cache.get::<Vec<SearchResult>>(&cache_key).await
        {
            info!("[CACHE HIT] {}", req.query);
            cached
        } else {
            info!("[CACHE MISS] {}", req.query);

            let strategy = self.factory.get(req.search_type)?;
            let api_results =
                strategy.search(&req.query, req.max_results).await?;

            self.cache.set(&cache_key, &api_results).await;
            api_results
        };

        // ── SEED ITEM CACHE (correct + safe) ─────────────────────
        for item in &results {
            let Some(id) = item.id() else {
                warn!("Skipping item without id: {:?}", item);
                continue;
            };

            let item_key =
                CacheService::item_key(req.search_type, &id);

            info!(
            "[SEED] type={:?} id={}",
            req.search_type, id
        );

            self.cache.set(&item_key, item).await;
        }

        Ok(results)
    }
    // ─── Find by ID (single item) ────────────────────────────────────────────

    /// FindById flow:
    /// 1. Build cache key from type + id
    /// 2. Check Redis — return immediately on hit
    /// 3. On miss: call the strategy (external API)
    /// 4. Store in Redis with 30-day TTL if found
    /// 5. Return result or None
    pub async fn find_by_id(
        &self,
        search_type: SearchType,
        id: &str,
    ) -> Result<Option<SearchResult>, AppError> {
        let cache_key = CacheService::item_key(search_type, id);

        // ── 1. Cache check ────────────────────────────────────────────────
        if let Some(cached) = self.cache.get::<SearchResult>(&cache_key).await {
            info!("[CACHE HIT] item id='{}' type={}", id, search_type);
            return Ok(Some(cached));
        }

        // ── 2. Cache miss → call external API ────────────────────────────
        info!(
            "[CACHE MISS] item id='{}' type={} — calling API",
            id, search_type
        );

        let strategy = self.factory.get(search_type)?;
        let result = strategy.find_by_id(id).await?;

        // ── 3. Store in Redis if we got a result (None = 404, don't cache) ─
        if let Some(ref item) = result {
            // FIX: `ref item` makes `item` a reference (`&SearchResult`).
            // Pass it directly without cloning.
            self.cache.set(&cache_key, item).await;
        } else {
            warn!(
                "[NOT FOUND] item id='{}' type={} — not caching",
                id, search_type
            );
        }

        Ok(result)
    }
}