mod common;

use axum::http::StatusCode;
use axum_test::TestServer;

#[tokio::test]
async fn health_returns_200_with_success_body() {
    let state = common::state_with_mock_cache().await;
    let app = common::build_app(state);
    let server = TestServer::new(app);

    let resp = server.get("/api/library/health").await;

    resp.assert_status(StatusCode::OK);

    let body: serde_json::Value = resp.json();
    assert_eq!(body["success"], true);
    assert_eq!(body["message"], "OK");
    assert!(body["data"].is_string());
}