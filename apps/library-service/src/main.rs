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
use axum::http::{HeaderValue, Method};
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

    use tokio::time::{sleep, Duration};

    let cache = loop {
        match CacheService::new(&redis_url).await {
            Ok(c) => {
                info!("Redis cache ready");
                break Arc::new(c);
            }
            Err(e) => {
                tracing::warn!(
                "Redis not ready yet ({}), retrying in 2s... error: {}",
                redis_url,
                e
            );
                sleep(Duration::from_secs(2)).await;
            }
        }
    };

    // ── Service factory ──────────────────────────────────────────────────────
    let factory = Arc::new(ServiceFactory::new());

    let state = AppState { factory, cache };

    // ── CORS ─────────────────────────────────────────────────────────────────
    let allowed_origins = std::env::var("CORS_ALLOWED_ORIGINS")
        .unwrap_or_else(|_| "http://localhost:23738".to_string());
    let origin_values: Vec<HeaderValue> = allowed_origins
        .split(',')
        .filter_map(|origin| {
            let trimmed = origin.trim();
            if trimmed.is_empty() {
                return None;
            }

            match HeaderValue::from_str(trimmed) {
                Ok(value) => Some(value),
                Err(error) => {
                    tracing::warn!("Skipping invalid CORS origin '{}': {}", trimmed, error);
                    None
                }
            }
        })
        .collect();

    let cors = CorsLayer::new()
        .allow_origin(origin_values)
        .allow_methods([
            Method::GET,
            Method::POST,
            Method::PUT,
            Method::DELETE,
            Method::OPTIONS,
        ])
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
