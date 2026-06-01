mod adapters;
mod cache;
mod dto;
mod error;
mod factory;
mod models;
mod routes;
mod service;
mod strategy;

use std::sync::Arc;

use axum::Router;
use tower_http::cors::{Any, CorsLayer};
use tower_http::trace::TraceLayer;
use tracing::info;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

use cache::CacheService;
use factory::ServiceFactory;
use routes::library_routes;

/// Application-wide shared state injected into every handler via Axum's State extractor.
#[derive(Clone)]
pub struct AppState {
    pub factory: Arc<ServiceFactory>,
    pub cache: Arc<CacheService>,
}

#[tokio::main]
async fn main() {
    // Load .env file if present
    dotenvy::dotenv().ok();

    // Initialize structured logging
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "library_service=debug,tower_http=info".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    // ── Redis cache ──────────────────────────────────────────────────────────
    let redis_url = std::env::var("REDIS_URL")
        .unwrap_or_else(|_| "redis://127.0.0.1:6379".to_string());

    let cache = match CacheService::new(&redis_url).await {
        Ok(c) => {
            info!("Redis cache ready");
            Arc::new(c)
        }
        Err(e) => {
            // Hard-fail on startup if Redis is unavailable.
            panic!("Failed to connect to Redis ({}): {}", redis_url, e);
        }
    };

    // ── Service factory ──────────────────────────────────────────────────────
    let factory = Arc::new(ServiceFactory::new());

    let state = AppState { factory, cache };

    // ── CORS ─────────────────────────────────────────────────────────────────
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let app = Router::new()
        .nest("/api/library", library_routes())
        .layer(TraceLayer::new_for_http())
        .layer(cors)
        .with_state(state);

    let addr = "0.0.0.0:28877";
    info!("Library microservice listening on http://{}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}
