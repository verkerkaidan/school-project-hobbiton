use axum::http::StatusCode;
use axum::response::IntoResponse;
use axum::body::to_bytes;
use axum::response::Response;
use serde_json::Value;

use library_service::error::AppError;

/// Helper: extract JSON body from Axum Response (ASYNC SAFE)
async fn json_body(resp: Response) -> Value {
    let bytes = to_bytes(resp.into_body(), usize::MAX)
        .await
        .expect("body read failed");

    serde_json::from_slice(&bytes).expect("body is not JSON")
}

#[tokio::test]
async fn not_found_status_and_body() {
    let resp = AppError::NotFound("book 42 not found".into()).into_response();

    assert_eq!(resp.status(), StatusCode::NOT_FOUND);

    let body = json_body(resp).await;

    assert_eq!(body["success"], false);
    assert!(body["message"].as_str().unwrap().contains("book 42 not found"));
    assert_eq!(body["totalResults"], 0);
    assert!(body["data"].is_null());
}

#[tokio::test]
async fn unknown_search_type_status_and_body() {
    let resp = AppError::UnknownSearchType("PODCAST".into()).into_response();

    assert_eq!(resp.status(), StatusCode::BAD_REQUEST);

    let body = json_body(resp).await;

    assert_eq!(body["success"], false);
    assert!(body["message"].as_str().unwrap().contains("PODCAST"));
}

#[tokio::test]
async fn cache_error_status_and_body() {
    let resp = AppError::Cache("connection refused".into()).into_response();

    assert_eq!(resp.status(), StatusCode::INTERNAL_SERVER_ERROR);

    let body = json_body(resp).await;

    assert_eq!(body["success"], false);
}

#[tokio::test]
async fn internal_error_status_and_body() {
    let resp = AppError::Internal(anyhow::anyhow!("unexpected panic")).into_response();

    assert_eq!(resp.status(), StatusCode::INTERNAL_SERVER_ERROR);

    let body = json_body(resp).await;

    assert_eq!(body["success"], false);
    assert!(body["message"].as_str().unwrap().contains("unexpected panic"));
}

#[tokio::test]
async fn error_response_always_has_timestamp() {
    let resp = AppError::Cache("test".into()).into_response();

    let body = json_body(resp).await;

    let ts = body["timestamp"]
        .as_str()
        .expect("timestamp must be a string");

    assert!(!ts.is_empty());
}