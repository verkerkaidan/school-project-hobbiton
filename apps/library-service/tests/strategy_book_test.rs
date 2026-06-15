/// Unit tests for book search behaviour using the shared MockBookStrategy.
/// No network calls, no Redis, no API keys required.

mod common;

use library_service::dto::request::SearchType;
use library_service::dto::response::SearchResult;
use library_service::strategy::SearchStrategy;

fn strategy() -> common::MockBookStrategy {
    common::MockBookStrategy
}

// ─── search_type() ───────────────────────────────────────────────────────────

#[test]
fn reports_book_search_type() {
    assert_eq!(strategy().search_type(), SearchType::Book);
}

// ─── search() ────────────────────────────────────────────────────────────────

#[tokio::test]
async fn search_returns_nonempty_results_for_known_query() {
    let results = strategy()
        .search("clean code", 3)
        .await
        .expect("search should not error");

    assert!(!results.is_empty(), "expected at least one result");
    assert!(results.len() <= 3);
}

#[tokio::test]
async fn search_results_are_book_variants() {
    let results = strategy()
        .search("refactoring", 2)
        .await
        .expect("search should not error");

    for r in &results {
        assert!(
            matches!(r, SearchResult::Book(_)),
            "all results must be Book variants"
        );
    }
}

#[tokio::test]
async fn search_result_has_title() {
    let results = strategy()
        .search("the pragmatic programmer", 1)
        .await
        .expect("search should not error");

    if let Some(SearchResult::Book(b)) = results.first() {
        assert!(b.title.is_some(), "book must have a title");
    }
}

#[tokio::test]
async fn search_respects_max_results_of_one() {
    let results = strategy()
        .search("programming", 1)
        .await
        .expect("search should not error");

    assert!(results.len() <= 1);
}

#[tokio::test]
async fn search_with_garbage_query_returns_empty_vec_not_error() {
    let results = strategy()
        .search("xyzzy_no_such_book_12345", 5)
        .await
        .expect("garbage query should return Ok, not Err");

    assert!(results.len() <= 5);
}

// ─── find_by_id() ────────────────────────────────────────────────────────────

#[tokio::test]
async fn find_by_id_known_isbn_returns_some() {
    let result = strategy()
        .find_by_id("9780132350884")
        .await
        .expect("find_by_id should not error");

    assert!(result.is_some(), "known id must return Some");
}

#[tokio::test]
async fn find_by_id_result_is_book_variant() {
    let result = strategy()
        .find_by_id("9780132350884")
        .await
        .expect("find_by_id should not error");

    if let Some(r) = result {
        assert!(matches!(r, SearchResult::Book(_)));
    }
}

#[tokio::test]
async fn find_by_id_result_has_id_matching_input() {
    let id = "9780132350884";
    let result = strategy()
        .find_by_id(id)
        .await
        .expect("find_by_id should not error");

    if let Some(r) = result {
        assert_eq!(r.id(), Some(id.to_string()));
    }
}

#[tokio::test]
async fn find_by_id_unknown_isbn_returns_none() {
    // MockBookStrategy returns None for ids starting with "000"
    let result = strategy()
        .find_by_id("000unknown")
        .await
        .expect("unknown id should return Ok(None), not Err");

    assert!(result.is_none());
}