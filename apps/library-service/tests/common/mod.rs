use std::collections::HashMap;
use std::sync::Arc;

use async_trait::async_trait;
use axum::Router;
use axum::http::{HeaderValue, Method};
use tower_http::cors::{Any, CorsLayer};

use library_service::{
    AppState,
    cache::CacheService,
    db::DbService,
    dto::request::SearchType,
    dto::response::{BookResponse, ExerciseResponse, MovieResponse, SearchResult},
    error::AppError,
    factory::ServiceFactory,
    routes::library_routes,
    strategy::SearchStrategy,
};

// ─────────────────────────────────────────────────────────────────────────────
// MOCK STRATEGIES
// ─────────────────────────────────────────────────────────────────────────────

pub struct MockBookStrategy;

#[async_trait]
impl SearchStrategy for MockBookStrategy {
    fn search_type(&self) -> SearchType { SearchType::Book }

    async fn search(&self, _query: &str, max_results: usize) -> Result<Vec<SearchResult>, AppError> {
        let results = (0..max_results.min(1))
            .map(|_| SearchResult::Book(BookResponse {
                id: Some("mock-123".to_string()),
                title: Some("Mock Rust Book".to_string()),
                authors: vec!["Test Author".to_string()],
                ..Default::default()
            }))
            .collect();
        Ok(results)
    }

    async fn find_by_id(&self, id: &str) -> Result<Option<SearchResult>, AppError> {
        if id.starts_with("000") { return Ok(None); }
        Ok(Some(SearchResult::Book(BookResponse {
            id: Some(id.to_string()),
            title: Some("Mock Rust Book".to_string()),
            ..Default::default()
        })))
    }
}

pub struct MockMovieStrategy;

#[async_trait]
impl SearchStrategy for MockMovieStrategy {
    fn search_type(&self) -> SearchType { SearchType::Movie }

    async fn search(&self, _query: &str, max_results: usize) -> Result<Vec<SearchResult>, AppError> {
        let results = (0..max_results.min(1))
            .map(|_| SearchResult::Movie(MovieResponse {
                id: Some("tt0000001".to_string()),
                title: Some("Mock Movie".to_string()),
                year: Some("2024".to_string()),
                ..Default::default()
            }))
            .collect();
        Ok(results)
    }

    async fn find_by_id(&self, id: &str) -> Result<Option<SearchResult>, AppError> {
        if id.starts_with("000") { return Ok(None); }
        Ok(Some(SearchResult::Movie(MovieResponse {
            id: Some(id.to_string()),
            title: Some("Mock Movie".to_string()),
            ..Default::default()
        })))
    }
}

pub struct MockExerciseStrategy;

#[async_trait]
impl SearchStrategy for MockExerciseStrategy {
    fn search_type(&self) -> SearchType { SearchType::Exercise }

    async fn search(&self, _query: &str, max_results: usize) -> Result<Vec<SearchResult>, AppError> {
        let results = (0..max_results.min(1))
            .map(|_| SearchResult::Exercise(ExerciseResponse {
                exercise_id: Some("mock-ex-1".to_string()),
                name: Some("Mock Squat".to_string()),
                gif_url: Some("https://example.com/mock.gif".to_string()),
                target_muscles: Some(vec!["quadriceps".to_string()]),
                instructions: Some(vec!["Stand up. Sit down.".to_string()]),
                ..Default::default()
            }))
            .collect();
        Ok(results)
    }

    async fn find_by_id(&self, id: &str) -> Result<Option<SearchResult>, AppError> {
        if id.starts_with("000") { return Ok(None); }
        Ok(Some(SearchResult::Exercise(ExerciseResponse {
            exercise_id: Some(id.to_string()),
            name: Some("Mock Squat".to_string()),
            gif_url: Some("https://example.com/mock.gif".to_string()),
            target_muscles: Some(vec!["quadriceps".to_string()]),
            instructions: Some(vec!["Stand up. Sit down.".to_string()]),
            ..Default::default()
        })))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APP BUILDER
// ─────────────────────────────────────────────────────────────────────────────

pub fn build_app(state: AppState) -> Router {
    let cors = CorsLayer::new()
        .allow_origin("http://localhost:23738".parse::<HeaderValue>().unwrap())
        .allow_methods([Method::GET, Method::POST])
        .allow_headers(Any);

    Router::new()
        .nest("/api/library", library_routes())
        .layer(cors)
        .with_state(state)
}

// ─────────────────────────────────────────────────────────────────────────────
// DB HELPER — connects if DATABASE_URL is set, otherwise skips
// ─────────────────────────────────────────────────────────────────────────────

pub async fn try_build_db() -> Arc<DbService> {
    match std::env::var("DATABASE_URL") {
        Ok(url) => Arc::new(
            DbService::new(&url)
                .await
                .expect("DATABASE_URL set but could not connect to Postgres"),
        ),
        Err(_) => Arc::new(DbService::disabled()),
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REDIS HELPERS
// ─────────────────────────────────────────────────────────────────────────────

pub async fn try_connect_redis() -> Option<Arc<CacheService>> {
    let url = std::env::var("REDIS_URL")
        .unwrap_or_else(|_| "redis://127.0.0.1:6379".to_string());
    match CacheService::new(&url).await {
        Ok(cache) => Some(Arc::new(cache)),
        Err(_) => None,
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APP STATE FACTORIES
// ─────────────────────────────────────────────────────────────────────────────

pub async fn state_with_mock_cache() -> AppState {
    let cache = build_cache().await;
    let factory = build_mock_factory();
    let db = try_build_db().await;
    AppState { factory, cache, db }
}

pub async fn state_with_live_cache() -> AppState {
    let url = std::env::var("REDIS_URL")
        .unwrap_or_else(|_| "redis://127.0.0.1:6379".to_string());
    let cache = Arc::new(
        CacheService::new(&url)
            .await
            .expect("Redis must be available for this test"),
    );
    let db = try_build_db().await;
    AppState {
        factory: build_mock_factory(),
        cache,
        db,
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INTERNAL HELPERS
// ─────────────────────────────────────────────────────────────────────────────

fn build_mock_factory() -> Arc<ServiceFactory> {
    let mut strategies: HashMap<SearchType, Arc<dyn SearchStrategy>> = HashMap::new();
    strategies.insert(SearchType::Book, Arc::new(MockBookStrategy));
    strategies.insert(SearchType::Movie, Arc::new(MockMovieStrategy));
    strategies.insert(SearchType::Exercise, Arc::new(MockExerciseStrategy));
    Arc::new(ServiceFactory::new_with(strategies))
}

async fn build_cache() -> Arc<CacheService> {
    let url = std::env::var("REDIS_URL")
        .unwrap_or_else(|_| "redis://127.0.0.1:6379".to_string());
    match CacheService::new(&url).await {
        Ok(c) => Arc::new(c),
        Err(_) => Arc::new(CacheService::disabled()),
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TEST UTILITIES
// ─────────────────────────────────────────────────────────────────────────────

pub async fn invalidate_item_key(state: &AppState, search_type: SearchType, id: &str) {
    let key = CacheService::item_key(search_type, id);
    state.cache.invalidate(&key).await;
}