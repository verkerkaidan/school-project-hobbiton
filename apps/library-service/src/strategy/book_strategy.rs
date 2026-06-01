use async_trait::async_trait;
use reqwest::Client;
use serde_json::Value;
use tracing::{info, warn};

use crate::adapters::book_adapter::{adapt_isbn_doc, adapt_search_doc, book_to_search_result};
use crate::dto::request::SearchType;
use crate::dto::response::SearchResult;
use crate::error::AppError;
use crate::strategy::SearchStrategy;

const BASE_URL: &str = "https://openlibrary.org";

/// **Strategy** implementation for the Open Library API.
///
/// Covers:
/// - `search()` → GET /search.json?q={query}&limit={max}
/// - `find_by_id()` → GET /api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data
///
/// No API key required — Open Library is fully open.
pub struct OpenLibraryStrategy {
    client: Client,
}

impl OpenLibraryStrategy {
    pub fn new() -> Self {
        OpenLibraryStrategy {
            client: Client::builder()
                .user_agent("Mozilla/5.0 library-microservice/1.0")
                .build()
                .expect("Failed to build HTTP client"),
        }
    }
}

#[async_trait]
impl SearchStrategy for OpenLibraryStrategy {
    fn search_type(&self) -> SearchType {
        SearchType::Book
    }

    async fn search(&self, query: &str, max_results: usize) -> Result<Vec<SearchResult>, AppError> {
        let url = format!("{}/search.json", BASE_URL);
        info!("OpenLibrary search: query='{}' max={}", query, max_results);

        let resp = self
            .client
            .get(&url)
            .query(&[("q", query), ("limit", &max_results.to_string())])
            .send()
            .await?
            .error_for_status()?;

        let body: Value = resp.json().await?;
        let docs = body["docs"].as_array().cloned().unwrap_or_default();

        let results = docs
            .iter()
            .map(|doc| book_to_search_result(adapt_search_doc(doc)))
            .collect();

        Ok(results)
    }

    async fn find_by_id(&self, isbn: &str) -> Result<Option<SearchResult>, AppError> {
        let url = format!("{}/api/books", BASE_URL);
        let bibkey = format!("ISBN:{}", isbn);
        info!("OpenLibrary ISBN lookup: {}", bibkey);

        let resp = self
            .client
            .get(&url)
            .query(&[
                ("bibkeys", bibkey.as_str()),
                ("format", "json"),
                ("jscmd", "data"),
            ])
            .send()
            .await?
            .error_for_status()?;

        let body: Value = resp.json().await?;

        if let Some(data) = body.get(&bibkey) {
            let book = adapt_isbn_doc(isbn, data);
            return Ok(Some(book_to_search_result(book)));
        }

        warn!("OpenLibrary: no result for ISBN {}", isbn);
        Ok(None)
    }
}
