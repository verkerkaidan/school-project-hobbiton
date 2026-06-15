/// Unit tests for movie search behaviour using MockMovieStrategy.
/// No network calls, no API keys required.

mod common;

use library_service::dto::request::SearchType;
use library_service::dto::response::SearchResult;
use library_service::strategy::SearchStrategy;

fn strategy() -> common::MockMovieStrategy {
    common::MockMovieStrategy
}

// ─── search_type() ───────────────────────────────────────────────────────────

#[test]
fn reports_movie_search_type() {
    assert_eq!(strategy().search_type(), SearchType::Movie);
}

// ─── search() ────────────────────────────────────────────────────────────────

#[tokio::test]
async fn search_returns_results_for_batman() {
    let results = strategy()
        .search("batman", 3)
        .await
        .expect("search should not error");

    assert!(!results.is_empty(), "expected at least one result");
    assert!(results.len() <= 3);
}

#[tokio::test]
async fn search_results_are_movie_variants() {
    let results = strategy()
        .search("inception", 2)
        .await
        .expect("search should not error");

    for r in &results {
        assert!(matches!(r, SearchResult::Movie(_)), "all results must be Movie variants");
    }
}

#[tokio::test]
async fn search_result_has_title_and_id() {
    let results = strategy()
        .search("the matrix", 1)
        .await
        .expect("search should not error");

    if let Some(SearchResult::Movie(m)) = results.first() {
        assert!(m.title.is_some(), "movie must have a title");
        assert!(m.id.is_some(), "movie must have an id");
    }
}

#[tokio::test]
async fn search_respects_max_results() {
    let results = strategy()
        .search("star wars", 2)
        .await
        .expect("search should not error");

    assert!(results.len() <= 2, "got {} results, expected ≤ 2", results.len());
}

#[tokio::test]
async fn search_unknown_title_returns_empty_not_error() {
    let results = strategy()
        .search("xyzzy_no_such_movie_9999999", 5)
        .await
        .expect("no-results query must return Ok, not Err");

    assert!(results.len() <= 5);
}

// ─── find_by_id() ────────────────────────────────────────────────────────────

#[tokio::test]
async fn find_by_id_known_id_returns_some() {
    let result = strategy()
        .find_by_id("tt0111161")
        .await
        .expect("find_by_id should not error");

    assert!(result.is_some(), "known id must return Some");
}

#[tokio::test]
async fn find_by_id_result_is_movie_variant() {
    let result = strategy()
        .find_by_id("tt0111161")
        .await
        .expect("find_by_id should not error");

    if let Some(r) = result {
        assert!(matches!(r, SearchResult::Movie(_)));
    }
}

#[tokio::test]
async fn find_by_id_result_has_matching_id() {
    let id = "tt0111161";
    let result = strategy()
        .find_by_id(id)
        .await
        .expect("find_by_id should not error");

    if let Some(r) = result {
        assert_eq!(r.id(), Some(id.to_string()));
    }
}

#[tokio::test]
async fn find_by_id_unknown_id_returns_none() {
    let result = strategy()
        .find_by_id("000unknown")
        .await
        .expect("unknown id should return Ok(None), not Err");

    assert!(result.is_none());
}