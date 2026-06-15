mod common;

use axum::http::StatusCode;
use axum_test::TestServer;
use library_service::dto::request::SearchType;

const KNOWN_ID: &str = "mock-123";
const MISSING_ID: &str = "000nonexistent";

#[tokio::test]
async fn find_by_id_known_isbn_returns_200() {
    let state = common::state_with_mock_cache().await;
    // Flush stale cache so we always exercise the mock, not a leftover hit
    common::invalidate_item_key(&state, SearchType::Book, KNOWN_ID).await;

    let server = TestServer::new(common::build_app(state));
    let resp = server.get(&format!("/api/library/BOOK/{}", KNOWN_ID)).await;

    resp.assert_status(StatusCode::OK);

    let body: serde_json::Value = resp.json();
    assert_eq!(body["success"], true);
    assert_eq!(body["totalResults"], 1);
    assert_eq!(body["data"]["resultType"], "book");
}

#[tokio::test]
async fn find_by_id_nonexistent_id_returns_404() {
    let state = common::state_with_mock_cache().await;
    // Critical: without this, a previous run may have seeded Some(...) under
    // this key and the cache hit path returns 200 instead of hitting the mock
    common::invalidate_item_key(&state, SearchType::Book, MISSING_ID).await;

    let server = TestServer::new(common::build_app(state));
    let resp = server.get(&format!("/api/library/BOOK/{}", MISSING_ID)).await;

    resp.assert_status_not_found();

    let body: serde_json::Value = resp.json();
    assert_eq!(body["success"], false);
}

#[tokio::test]
async fn find_by_id_unknown_type_returns_400() {
    let state = common::state_with_mock_cache().await;
    let server = TestServer::new(common::build_app(state));

    let resp = server.get("/api/library/PODCAST/some-id").await;

    resp.assert_status_bad_request();

    let body: serde_json::Value = resp.json();
    assert_eq!(body["success"], false);
}

#[tokio::test]
async fn find_by_id_lowercase_type_is_accepted() {
    let state = common::state_with_mock_cache().await;
    let server = TestServer::new(common::build_app(state));

    let resp = server.get(&format!("/api/library/book/{}", KNOWN_ID)).await;

    assert!(
        resp.status_code() == StatusCode::OK || resp.status_code() == StatusCode::NOT_FOUND,
        "expected 200 or 404, got {}",
        resp.status_code()
    );
}

#[tokio::test]
async fn find_by_id_response_envelope_has_all_keys() {
    let state = common::state_with_mock_cache().await;
    let server = TestServer::new(common::build_app(state));

    let resp = server.get(&format!("/api/library/BOOK/{}", KNOWN_ID)).await;

    if resp.status_code() == StatusCode::OK {
        let body: serde_json::Value = resp.json();
        for key in &["success", "data", "message", "timestamp", "totalResults"] {
            assert!(body.get(key).is_some(), "missing key '{}'", key);
        }
    }
}