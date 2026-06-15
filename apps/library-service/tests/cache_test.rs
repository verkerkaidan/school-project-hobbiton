/// Integration tests for `CacheService`.
///
/// These tests require a running Redis instance. When Redis is unavailable
/// the tests skip themselves via the `require_redis!` macro so CI never
/// fails due to a missing service.
///
/// Run with a local Redis:
///   redis-server &
///   cargo test --test cache_test

use library_service::{
    cache::CacheService,
    dto::request::SearchType,
};

// ─── Skip-guard ───────────────────────────────────────────────────────────────

/// Returns a live `CacheService` or skips the test.
macro_rules! require_redis {
    () => {{
        let url = std::env::var("REDIS_URL")
            .unwrap_or_else(|_| "redis://127.0.0.1:6379".to_string());
        match CacheService::new(&url).await {
            Ok(c) => c,
            Err(_) => {
                eprintln!("[SKIP] Redis not available at {}", url);
                return;
            }
        }
    }};
}

// ─── Key builders (pure, no Redis needed) ────────────────────────────────────

#[test]
fn search_key_format_book() {
    let key = CacheService::search_key(SearchType::Book, "clean code", 10);
    assert_eq!(key, "search:BOOK:clean code:10");
}

#[test]
fn search_key_trims_and_lowercases() {
    let key = CacheService::search_key(SearchType::Movie, "  Batman  ", 5);
    assert_eq!(key, "search:MOVIE:batman:5");
}

#[test]
fn item_key_format_movie() {
    let key = CacheService::item_key(SearchType::Movie, "tt0111161");
    assert_eq!(key, "item:MOVIE:tt0111161");
}

#[test]
fn item_key_format_book() {
    let key = CacheService::item_key(SearchType::Book, "9780132350884");
    assert_eq!(key, "item:BOOK:9780132350884");
}

#[test]
fn item_key_format_exercise() {
    let key = CacheService::item_key(SearchType::Exercise, "42");
    assert_eq!(key, "item:EXERCISE:42");
}

// ─── Live Redis round-trip tests ──────────────────────────────────────────────

#[tokio::test]
async fn set_then_get_returns_same_value() {
    let cache = require_redis!();

    let key = "test:roundtrip:string";
    cache.set(key, &"hello world".to_string()).await;

    let result: Option<String> = cache.get(key).await;
    assert_eq!(result, Some("hello world".to_string()));

    cache.invalidate(key).await;
}

#[tokio::test]
async fn get_missing_key_returns_none() {
    let cache = require_redis!();

    let key = "test:definitely:does:not:exist:xyzzy";
    let result: Option<String> = cache.get(key).await;
    assert_eq!(result, None);
}

#[tokio::test]
async fn invalidate_removes_key() {
    let cache = require_redis!();

    let key = "test:invalidate";
    cache.set(key, &42u64).await;

    // Confirm it's there.
    let before: Option<u64> = cache.get(key).await;
    assert_eq!(before, Some(42));

    cache.invalidate(key).await;

    let after: Option<u64> = cache.get(key).await;
    assert_eq!(after, None);
}

#[tokio::test]
async fn can_cache_a_vec_of_strings() {
    let cache = require_redis!();

    let key = "test:vec:strings";
    let data: Vec<String> = vec!["one".into(), "two".into(), "three".into()];

    cache.set(key, &data).await;

    let result: Option<Vec<String>> = cache.get(key).await;
    assert_eq!(result, Some(data));

    cache.invalidate(key).await;
}

#[tokio::test]
async fn overwrite_replaces_previous_value() {
    let cache = require_redis!();

    let key = "test:overwrite";
    cache.set(key, &"first".to_string()).await;
    cache.set(key, &"second".to_string()).await;

    let result: Option<String> = cache.get(key).await;
    assert_eq!(result, Some("second".to_string()));

    cache.invalidate(key).await;
}

#[tokio::test]
async fn ttl_constant_is_thirty_days_in_seconds() {
    // Regression guard: changing CACHE_TTL_SECONDS accidentally would affect all users.
    assert_eq!(
        library_service::cache::CACHE_TTL_SECONDS,
        30 * 24 * 60 * 60,
        "TTL must be exactly 30 days"
    );
}