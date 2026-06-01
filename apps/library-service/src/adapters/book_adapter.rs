use serde_json::Value;
use crate::models::book::Book;
use crate::dto::response::{BookResponse, SearchResult};

/// **Adapter Pattern**
///
/// Converts the raw JSON structures returned by the Open Library API
/// into the internal `Book` domain model and the `BookResponse` DTO.
///
/// The rest of the codebase never sees raw Open Library JSON.

/// Adapt a single Open Library "search doc" JSON object → `Book`.
pub fn adapt_search_doc(raw: &Value) -> Book {
    Book {
        id: raw["key"].as_str().map(str::to_owned),
        title: raw["title"].as_str().map(str::to_owned),
        authors: json_str_array(&raw["authors"]),
        publishers: json_str_array(&raw["publishers"]),
        isbn: json_str_array(&raw["isbn"]),
        languages: json_str_array(&raw["languages"]),
        published_date: raw["first_published_year"]
            .as_i64()
            .map(|t|t.to_string())
            .or_else(|| raw["first_published_year"].as_str().map(str::to_owned)),
        page_count: raw["number_of_pages_median"].as_u64().map(|n| n as u32),
        cover_id: raw["cover_i"].as_i64().map(|id| id.to_string()),
        thumbnail_url: raw["cover_i"].as_i64().map(|id| format!("https://covers.openlibrary.org/b/id/{}-M.jpg", id)),
    }
}

pub fn book_to_search_result(book: Book) -> SearchResult {
    SearchResult::Book(BookResponse {
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

pub fn adapt_isbn_doc(isbn: &str, data: &Value) -> Book {
    let mut book = Book::default();
    book.isbn = vec![isbn.to_owned()];
    book.title = data["title"].as_str().map(str::to_owned);

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

    book
}

fn json_str_array(json: &Value) -> Vec<String> {
    json.as_array().map(|arr| {
        arr.iter().filter_map(|x| x.as_str().map(|y| y.to_owned())).collect()
    })
        .unwrap_or_default()
}