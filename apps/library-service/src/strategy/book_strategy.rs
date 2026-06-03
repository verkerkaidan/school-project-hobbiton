use async_trait::async_trait;
use reqwest::Client;
use serde_json::Value;
use tracing::{info, warn};

use crate::adapters::book_adapter::{adapt_isbn_doc, adapt_search_doc, adapt_work_doc, book_to_search_result};
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

    /// Fetch full book/work details from OpenLibrary using the work key.
    async fn fetch_work_details(&self, key: &str) -> Result<Option<SearchResult>, AppError> {
        // Format: key = "/works/OL123W" → convert to "OL123W"
        let key_id = key.split('/').last().unwrap_or(key);
        let url = format!("{}/works/{}.json", BASE_URL, key_id);

        let resp = self.client.get(&url).send().await?;

        if resp.status() == reqwest::StatusCode::NOT_FOUND {
            return Ok(None);
        }

        let body: Value = resp.error_for_status()?.json().await?;
        let book = adapt_work_doc(&body);
        Ok(Some(book_to_search_result(book)))
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

        // Request edition-level fields so we can populate ISBN, publish_date, languages etc.
        // Keep the set reasonably small to avoid very large responses.
        let fields = "key,title,author_name,publisher,isbn,language,first_publish_year,number_of_pages_median,cover_i,editions,editions.key,editions.isbn,editions.publish_date,editions.number_of_pages";

        let resp = self
            .client
            .get(&url)
            .query(&[("q", query), ("limit", &max_results.to_string()), ("fields", fields)])
            .send()
            .await?
            .error_for_status()?;

        let body: Value = resp.json().await?;
        let docs = body["docs"].as_array().cloned().unwrap_or_default();

        let mut results = Vec::new();

        for doc in docs.into_iter().take(max_results) {
            // Start from the adapted search doc (work-level fields)
            let mut book = adapt_search_doc(&doc);

            // If editions subobject exists and contains docs, prefer data from the first edition
            if let Some(editions) = doc.get("editions") {
                if let Some(ed_docs) = editions.get("docs").and_then(|v| v.as_array()) {
                    if let Some(first_ed) = ed_docs.first() {
                        // override ISBNs if present
                        if let Some(isbns) = first_ed.get("isbn").and_then(|v| v.as_array()) {
                            book.isbn = isbns.iter().filter_map(|s| s.as_str().map(str::to_owned)).collect();
                        }
                        // override published date if present
                        if let Some(pub_date) = first_ed.get("publish_date").and_then(|v| v.as_str()) {
                            book.published_date = Some(pub_date.to_string());
                        }
                        // override page count
                        if let Some(n) = first_ed.get("number_of_pages").and_then(|v| v.as_u64()) {
                            book.page_count = Some(n as u32);
                        }
                        // override languages
                        if let Some(langs) = first_ed.get("language").and_then(|v| v.as_array()) {
                            book.languages = langs.iter().filter_map(|s| s.as_str().map(str::to_owned)).collect();
                        }
                    }
                }
            }

            results.push(book_to_search_result(book));
        }

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
