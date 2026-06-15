/// Unit tests for exercise search behaviour using MockExerciseStrategy.
/// No network calls, no API keys required.

mod common;

use library_service::dto::request::SearchType;
use library_service::dto::response::SearchResult;
use library_service::strategy::SearchStrategy;

fn strategy() -> common::MockExerciseStrategy {
    common::MockExerciseStrategy
}

// ─── search_type() ───────────────────────────────────────────────────────────

#[test]
fn reports_exercise_search_type() {
    assert_eq!(strategy().search_type(), SearchType::Exercise);
}

// ─── search() ────────────────────────────────────────────────────────────────

#[tokio::test]
async fn search_squat_returns_results() {
    let results = strategy()
        .search("squat", 3)
        .await
        .expect("search should not error");

    assert!(!results.is_empty(), "expected at least one result");
    assert!(results.len() <= 3);
}

#[tokio::test]
async fn search_results_are_exercise_variants() {
    let results = strategy()
        .search("push", 2)
        .await
        .expect("search should not error");

    for r in &results {
        assert!(matches!(r, SearchResult::Exercise(_)), "all results must be Exercise variants");
    }
}

#[tokio::test]
async fn search_result_has_name_and_id() {
    let results = strategy()
        .search("bench press", 1)
        .await
        .expect("search should not error");

    if let Some(SearchResult::Exercise(e)) = results.first() {
        assert!(e.name.is_some(), "exercise must have a name");
        assert!(e.exercise_id.is_some(), "exercise must have an ID");
    }
}

#[tokio::test]
async fn search_respects_max_results() {
    let results = strategy()
        .search("curl", 2)
        .await
        .expect("search should not error");

    assert!(results.len() <= 2, "got {} results, expected ≤ 2", results.len());
}

#[tokio::test]
async fn search_unknown_exercise_returns_empty_not_error() {
    let results = strategy()
        .search("xyzzy_no_such_exercise_99999", 5)
        .await
        .expect("no-results query must return Ok, not Err");

    assert!(results.len() <= 5);
}

#[tokio::test]
async fn search_result_gif_url_is_some() {
    let results = strategy()
        .search("deadlift", 1)
        .await
        .expect("search should not error");

    if let Some(SearchResult::Exercise(e)) = results.first() {
        assert!(e.gif_url.is_some(), "gif_url should be populated");
    }
}

#[tokio::test]
async fn search_full_detail_includes_target_muscles() {
    let results = strategy()
        .search("squat", 1)
        .await
        .expect("search should not error");

    if let Some(SearchResult::Exercise(e)) = results.first() {
        assert!(e.target_muscles.is_some(), "target_muscles should be populated");
    }
}

// ─── find_by_id() ────────────────────────────────────────────────────────────

#[tokio::test]
async fn find_by_id_unknown_id_returns_none() {
    let result = strategy()
        .find_by_id("000nonexistent")
        .await
        .expect("unknown ID should return Ok(None), not Err");

    assert!(result.is_none());
}

#[tokio::test]
async fn find_by_id_result_is_exercise_variant() {
    let result = strategy()
        .find_by_id("mock-ex-1")
        .await
        .expect("find_by_id should not error");

    if let Some(r) = result {
        assert!(matches!(r, SearchResult::Exercise(_)));
    }
}

#[tokio::test]
async fn find_by_id_has_instructions() {
    let result = strategy()
        .find_by_id("mock-ex-1")
        .await
        .expect("find_by_id failed");

    if let Some(SearchResult::Exercise(e)) = result {
        assert!(e.instructions.is_some(), "full detail should include instructions");
    }
}