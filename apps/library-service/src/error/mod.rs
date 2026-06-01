use axum::http::StatusCode;
use axum::Json;
use axum::response::{IntoResponse, Response};
use serde_json::json;
use thiserror::Error;

#[derive(Debug, Error)]
pub enum AppError {
    #[error("No service registered for search type: {0}")]
    UnknownSearchType(String),
    #[error("Resource not found: {0}")]
    NotFound(String),
    #[error("External API error: {0}")]
    ExternalApi(#[from] reqwest::Error),
    #[error("Cache error: {0}")]
    Cache(String),
    #[error("Internal error: {0}")]
    Internal(#[from] anyhow::Error),
}

impl IntoResponse for AppError {
    fn into_response(self) -> Response {
        let (status, message) = match &self {
            AppError::NotFound(msg) => (StatusCode::NOT_FOUND, msg.clone()),
            AppError::UnknownSearchType(msg) => (StatusCode::BAD_REQUEST, msg.clone()),
            AppError::ExternalApi(e) => (StatusCode::BAD_GATEWAY, e.to_string()),
            AppError::Cache(msg) => (StatusCode::INTERNAL_SERVER_ERROR, msg.clone()),
            AppError::Internal(e) => (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()),
        };

        let body = json!({
            "success": false,
            "message": message,
            "totalResults": 0,
            "data": null,
            "timestamp": chrono::Utc::now().to_rfc3339()
        });

        (status, Json(body)).into_response()
    }
}