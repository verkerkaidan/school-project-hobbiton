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

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;

    #[test]
    fn adapt_search_doc_maps_all_fields() {
        let raw = json!({
            "title": "Clean Code",
            "author_name": ["Robert C. Martin"],
            "publisher": ["Prentice Hall"],
            "isbn": ["9780132350884"],
            "language": ["eng"],
            "first_publish_year": 2008,
            "number_of_pages_median": 464,
            "cover_i": 12345
        });

        let book = adapt_search_doc(&raw);

        assert_eq!(book.id, Some("9780132350884".to_string()));
        assert_eq!(book.title, Some("Clean Code".to_string()));
        assert_eq!(book.authors, vec!["Robert C. Martin"]);
        assert_eq!(book.publishers, vec!["Prentice Hall"]);
        assert_eq!(book.languages, vec!["eng"]);
        assert_eq!(book.page_count, Some(464));
        assert_eq!(book.cover_id, Some("12345".to_string()));

        assert_eq!(
            book.thumbnail_url,
            Some("https://covers.openlibrary.org/b/id/12345-M.jpg".to_string())
        );
    }

    #[test]
    fn adapt_search_doc_falls_back_to_key_when_no_isbn() {
        let raw = json!({
            "key": "/works/OL123W",
            "title": "Test Book"
        });

        let book = adapt_search_doc(&raw);

        assert_eq!(book.id, Some("/works/OL123W".to_string()));
    }

    #[test]
    fn adapt_search_doc_handles_missing_fields() {
        let raw = json!({});

        let book = adapt_search_doc(&raw);

        assert_eq!(book.id, None);
        assert_eq!(book.title, None);
        assert!(book.authors.is_empty());
        assert!(book.publishers.is_empty());
        assert!(book.languages.is_empty());
        assert_eq!(book.page_count, None);
    }

    #[test]
    fn adapt_isbn_doc_maps_all_fields() {
        let raw = json!({
            "title": "Clean Code",
            "publish_date": "2008",
            "number_of_pages": 464,
            "cover": {
                "medium": "https://example.com/cover.jpg"
            },
            "authors": [
                { "name": "Robert C. Martin" }
            ],
            "publishers": [
                { "name": "Prentice Hall" }
            ],
            "languages": [
                { "name": "English" }
            ]
        });

        let book = adapt_isbn_doc("9780132350884", &raw);

        assert_eq!(book.id, Some("9780132350884".to_string()));
        assert_eq!(book.title, Some("Clean Code".to_string()));
        assert_eq!(book.page_count, Some(464));
        assert_eq!(
            book.thumbnail_url,
            Some("https://example.com/cover.jpg".to_string())
        );
        assert_eq!(book.authors, vec!["Robert C. Martin"]);
        assert_eq!(book.publishers, vec!["Prentice Hall"]);
        assert_eq!(book.languages, vec!["English"]);
    }

    #[test]
    fn json_str_array_returns_empty_for_non_array() {
        let value = json!({
            "foo": "bar"
        });

        let result = json_str_array(&value);

        assert!(result.is_empty());
    }
}