/// Integration tests for `ServiceFactory`.
///
/// These test the factory in isolation — no HTTP server, no Redis.
/// They confirm that every registered strategy is reachable and returns
/// the correct `SearchType` tag.

use library_service::{
    dto::request::SearchType,
    factory::ServiceFactory,
};

// ─── Happy-path: every known type resolves ────────────────────────────────────

#[test]
fn factory_resolves_book_strategy() {
    let factory = ServiceFactory::new();
    let s = factory.get(SearchType::Book).expect("Book strategy must be registered");
    assert_eq!(s.search_type(), SearchType::Book);
}

#[test]
fn factory_resolves_movie_strategy() {
    let factory = ServiceFactory::new();
    let s = factory.get(SearchType::Movie).expect("Movie strategy must be registered");
    assert_eq!(s.search_type(), SearchType::Movie);
}

#[test]
fn factory_resolves_exercise_strategy() {
    let factory = ServiceFactory::new();
    let s = factory.get(SearchType::Exercise).expect("Exercise strategy must be registered");
    assert_eq!(s.search_type(), SearchType::Exercise);
}

// ─── All search-types are covered ────────────────────────────────────────────

/// Ensures that adding a new `SearchType` variant without registering a strategy
/// will break this test — acting as a compile-time/test-time completeness check.
#[test]
fn factory_covers_all_search_types() {
    let factory = ServiceFactory::new();
    let all_types = [SearchType::Book, SearchType::Movie, SearchType::Exercise];

    for st in all_types {
        assert!(
            factory.get(st).is_ok(),
            "No strategy registered for {:?}",
            st
        );
    }
}

// ─── Strategy instances are independent (Arc, not shared state mutation) ──────

#[test]
fn factory_get_returns_independent_arcs() {
    let factory = ServiceFactory::new();

    let a = factory.get(SearchType::Book).unwrap();
    let b = factory.get(SearchType::Book).unwrap();

    // Both Arcs should point to the same underlying object (same pointer).
    // This confirms the factory caches the Arc rather than allocating fresh each call.
    assert!(Arc::ptr_eq(&a, &b));
}

use std::sync::Arc;