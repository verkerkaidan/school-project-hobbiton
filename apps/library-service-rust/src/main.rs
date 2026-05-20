use axum::{Router, routing::get, response::Json};
use serde_json::json;
use std::net::SocketAddr;
use tracing::info;
use tokio::net::TcpListener;

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt::init();

    let app = Router::new().route("/api/v1/library/health", get(health_handler));

    let addr = SocketAddr::from(([0,0,0,0], 28877));
    info!("Library microservice listening on http://{}", addr);

    let listener = TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

async fn health_handler() -> Json<serde_json::Value> {
    Json(json!({"success": true, "data": "Library Rust service running"}))
}