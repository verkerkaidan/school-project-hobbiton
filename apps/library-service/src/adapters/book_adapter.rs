use serde_json::Value;
use crate::models::book::Book;
use crate::dto::response::{BookResponse, SearchResult};
use tracing::debug;

/// **Adapter Pattern**
///
/// Converts the raw JSON structures returned by the Open Library API
/// into the internal `Book` domain model and the `BookResponse` DTO.
///
/// The rest of the codebase never sees raw Open Library JSON.

/// Adapt a single Open Library "search doc" JSON object → `Book`.
pub fn adapt_search_doc(raw: &Value) -> Book {
    debug!("Adapting OpenLibrary search doc: {}", raw);
    let isbn = json_str_array(&raw["isbn"]);
    let id = isbn.first().cloned().or_else(|| raw["key"].as_str().map(str::to_owned));

    Book {
        id,
        title: raw["title"].as_str().map(str::to_owned),
        authors: json_str_array(&raw["author_name"]),
        publishers: json_str_array(&raw["publisher"]),
        isbn,
        languages: json_str_array(&raw["language"]),
        published_date: raw["first_publish_year"]
            .as_i64()
            .map(|t| t.to_string())
            .or_else(|| raw["first_publish_year"].as_str().map(str::to_owned)),
        page_count: raw["number_of_pages_median"].as_u64().map(|n| n as u32),
        cover_id: raw["cover_i"].as_i64().map(|id| id.to_string()),
        thumbnail_url: raw["cover_i"].as_i64().map(|id| format!("https://covers.openlibrary.org/b/id/{}-M.jpg", id)),
    }
}

pub fn book_to_search_result(book: Book) -> SearchResult {
    SearchResult::Book(BookResponse {
        id: book.id,
        title: book.title,
        authors: book.authors,
        published_date: book.published_date,
        publishers: book.publishers,
        isbn: book.isbn,
        page_count: book.page_count,
        languages: book.languages,
        cover_id: book.cover_id.clone(),
        thumbnail_url: book.cover_id.as_ref().map(|id| format!("https://covers.openlibrary.org/b/id/{}-M.jpg", id)).or(book.thumbnail_url),
    })
}
/// Adapt a full OpenLibrary ISBN lookup details JSON → `Book`.
pub fn adapt_isbn_doc(isbn: &str, data: &Value) -> Book {
    let mut book = Book::default();
    book.id = Some(isbn.to_owned());
    book.isbn = vec![isbn.to_owned()];
    book.title = data["title"].as_str().map(str::to_owned);
    book.published_date = data["publish_date"].as_str().map(str::to_owned);
    book.page_count = data["number_of_pages"].as_u64().map(|n| n as u32);

    if let Some(cover) = data["cover"].as_object() {
        book.thumbnail_url = cover
            .get("medium")
            .and_then(|v| v.as_str())
            .map(str::to_owned);
    }

    if let Some(authors) = data["authors"].as_array() {
        book.authors = authors
            .iter()
            .filter_map(|a| a["name"].as_str().map(str::to_owned))
            .collect();
    }

    if let Some(publishers) = data["publishers"].as_array() {
        book.publishers = publishers
            .iter()
            .filter_map(|p| p["name"].as_str().map(str::to_owned))
            .collect();
    }

    if let Some(languages) = data["languages"].as_array() {
        book.languages = languages
            .iter()
            .filter_map(|l| l["name"].as_str().map(str::to_owned))
            .collect();
    }
    book
}

fn json_str_array(json: &Value) -> Vec<String> {
    json.as_array().map(|arr| {
        arr.iter().filter_map(|x| x.as_str().map(|y| y.to_owned())).collect()
    })
        .unwrap_or_default()
}