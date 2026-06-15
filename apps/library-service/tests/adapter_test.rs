/// Integration tests for the book adapter.
///
/// These supplement the unit tests in `src/adapters/book_adapter.rs` with
/// richer / more realistic JSON payloads and edge-case shapes that occur
/// in real Open Library API responses.

use library_service::adapters::book_adapter::{adapt_isbn_doc, adapt_search_doc, book_to_search_result};
use library_service::dto::response::SearchResult;
use serde_json::json;

// ─── adapt_search_doc ────────────────────────────────────────────────────────

#[test]
fn search_doc_with_multiple_isbns_uses_first_as_id() {
    let raw = json!({
        "isbn": ["111", "222", "333"],
        "title": "Multi-ISBN Book"
    });

    let book = adapt_search_doc(&raw);

    assert_eq!(book.id, Some("111".to_string()));
    assert_eq!(book.isbn.len(), 3);
}

#[test]
fn search_doc_prefers_isbn_over_key_for_id() {
    let raw = json!({
        "key": "/works/OL99W",
        "isbn": ["9780000000001"]
    });

    let book = adapt_search_doc(&raw);

    assert_eq!(book.id, Some("9780000000001".to_string()));
}

#[test]
fn search_doc_falls_back_to_key_when_isbn_array_empty() {
    let raw = json!({
        "key": "/works/OL999W",
        "isbn": []
    });

    let book = adapt_search_doc(&raw);

    assert_eq!(book.id, Some("/works/OL999W".to_string()));
}

#[test]
fn search_doc_published_year_as_integer() {
    let raw = json!({ "first_publish_year": 1999 });
    let book = adapt_search_doc(&raw);
    assert_eq!(book.published_date, Some("1999".to_string()));
}

#[test]
fn search_doc_published_year_as_string() {
    // Open Library sometimes returns year as a string in certain endpoints.
    let raw = json!({ "first_publish_year": "2001" });
    let book = adapt_search_doc(&raw);
    assert_eq!(book.published_date, Some("2001".to_string()));
}

#[test]
fn search_doc_cover_id_builds_thumbnail_url() {
    let raw = json!({ "cover_i": 98765 });
    let book = adapt_search_doc(&raw);
    assert_eq!(
        book.thumbnail_url,
        Some("https://covers.openlibrary.org/b/id/98765-M.jpg".to_string())
    );
    assert_eq!(book.cover_id, Some("98765".to_string()));
}

#[test]
fn search_doc_multiple_authors_are_all_captured() {
    let raw = json!({
        "author_name": ["Author One", "Author Two", "Author Three"]
    });
    let book = adapt_search_doc(&raw);
    assert_eq!(book.authors.len(), 3);
    assert!(book.authors.contains(&"Author Two".to_string()));
}

#[test]
fn search_doc_page_count_from_median() {
    let raw = json!({ "number_of_pages_median": 512 });
    let book = adapt_search_doc(&raw);
    assert_eq!(book.page_count, Some(512));
}

// ─── adapt_isbn_doc ──────────────────────────────────────────────────────────

#[test]
fn isbn_doc_id_is_always_provided_isbn() {
    let raw = json!({});
    let book = adapt_isbn_doc("9781234567890", &raw);
    assert_eq!(book.id, Some("9781234567890".to_string()));
    assert_eq!(book.isbn, vec!["9781234567890"]);
}

#[test]
fn isbn_doc_missing_cover_object_is_graceful() {
    let raw = json!({ "title": "No Cover Book" });
    let book = adapt_isbn_doc("123", &raw);
    assert_eq!(book.thumbnail_url, None);
}

#[test]
fn isbn_doc_authors_extracted_correctly() {
    let raw = json!({
        "authors": [
            { "name": "Alice" },
            { "name": "Bob" }
        ]
    });
    let book = adapt_isbn_doc("123", &raw);
    assert_eq!(book.authors, vec!["Alice", "Bob"]);
}

#[test]
fn isbn_doc_skips_authors_without_name_field() {
    let raw = json!({
        "authors": [
            { "name": "Valid Author" },
            { "key": "/authors/OL1A" }  // no "name"
        ]
    });
    let book = adapt_isbn_doc("123", &raw);
    assert_eq!(book.authors, vec!["Valid Author"]);
}

#[test]
fn isbn_doc_empty_publishers_list() {
    let raw = json!({ "publishers": [] });
    let book = adapt_isbn_doc("123", &raw);
    assert!(book.publishers.is_empty());
}

// ─── book_to_search_result ────────────────────────────────────────────────────

#[test]
fn book_to_search_result_wraps_as_book_variant() {
    let raw = json!({
        "isbn": ["9780132350884"],
        "title": "Clean Code",
        "cover_i": 12345
    });
    let book = adapt_search_doc(&raw);
    let result = book_to_search_result(book);

    assert!(matches!(result, SearchResult::Book(_)));
}

#[test]
fn book_to_search_result_id_propagated() {
    let raw = json!({ "isbn": ["9780000000000"] });
    let book = adapt_search_doc(&raw);
    let result = book_to_search_result(book);

    assert_eq!(result.id(), Some("9780000000000".to_string()));
}

#[test]
fn book_to_search_result_thumbnail_uses_cover_id_if_thumbnail_missing() {
    use library_service::models::book::Book;
    use library_service::dto::response::SearchResult;

    let book = Book {
        id: Some("X".into()),
        title: None,
        authors: vec![],
        publishers: vec![],
        isbn: vec![],
        languages: vec![],
        published_date: None,
        page_count: None,
        cover_id: Some("99999".into()),
        thumbnail_url: None, // no pre-built URL
    };

    let result = book_to_search_result(book);
    if let SearchResult::Book(b) = result {
        assert_eq!(
            b.thumbnail_url,
            Some("https://covers.openlibrary.org/b/id/99999-M.jpg".to_string())
        );
    } else {
        panic!("expected Book variant");
    }
}