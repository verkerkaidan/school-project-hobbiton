mod common;

use axum::http::StatusCode;
use axum_test::TestServer;
use serde_json::json;

// ─── POST /api/library/search ─────────────────────────────────────────────────

#[tokio::test]
async fn post_search_book_valid_body_returns_200() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .post("/api/library/search")
        .json(&json!({ "type": "BOOK", "query": "rust programming", "maxResults": 3 }))
        .await;

    resp.assert_status_ok();

    let body: serde_json::Value = resp.json();
    assert_eq!(body["success"], true);
    assert!(body["data"].is_array());
    assert!(body["data"].as_array().unwrap().len() <= 3);
}

#[tokio::test]
async fn post_search_missing_query_returns_422() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .post("/api/library/search")
        .json(&json!({ "type": "BOOK" }))
        .await;

    resp.assert_status(StatusCode::UNPROCESSABLE_ENTITY);
}

#[tokio::test]
async fn post_search_unknown_type_returns_422() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .post("/api/library/search")
        .json(&json!({ "type": "MUSIC", "query": "beethoven" }))
        .await;

    resp.assert_status(StatusCode::UNPROCESSABLE_ENTITY);
}

#[tokio::test]
async fn post_search_empty_body_returns_422() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .post("/api/library/search")
        .json(&json!({}))
        .await;

    resp.assert_status(StatusCode::UNPROCESSABLE_ENTITY);
}

#[tokio::test]
async fn post_search_default_max_results_is_ten() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .post("/api/library/search")
        .json(&json!({ "type": "BOOK", "query": "design patterns" }))
        .await;

    resp.assert_status_ok();

    let body: serde_json::Value = resp.json();
    let total = body["totalResults"].as_u64().unwrap_or(0);
    assert!(total <= 10, "expected ≤10 results, got {}", total);
}

// ─── GET /api/library/search ──────────────────────────────────────────────────

#[tokio::test]
async fn get_search_valid_params_returns_200() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .get("/api/library/search")
        .add_query_param("query", "clean code")
        .add_query_param("type", "BOOK")
        .add_query_param("maxResults", "5")
        .await;

    resp.assert_status_ok();

    let body: serde_json::Value = resp.json();
    assert_eq!(body["success"], true);
}

#[tokio::test]
async fn get_search_missing_type_returns_400() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .get("/api/library/search")
        .add_query_param("query", "clean code")
        .await;

    resp.assert_status(StatusCode::BAD_REQUEST);
}

#[tokio::test]
async fn get_search_missing_query_returns_400() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .get("/api/library/search")
        .add_query_param("type", "BOOK")
        .await;

    resp.assert_status(StatusCode::BAD_REQUEST);
}

#[tokio::test]
async fn get_search_response_has_expected_shape() {
    let server = TestServer::new(common::build_app(common::state_with_mock_cache().await));

    let resp = server
        .get("/api/library/search")
        .add_query_param("query", "refactoring")
        .add_query_param("type", "BOOK")
        .add_query_param("maxResults", "2")
        .await;

    resp.assert_status_ok();

    let body: serde_json::Value = resp.json();
    for key in &["success", "data", "message", "timestamp", "totalResults"] {
        assert!(body.get(key).is_some(), "missing key '{}'", key);
    }
}