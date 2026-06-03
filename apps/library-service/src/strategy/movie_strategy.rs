use async_trait::async_trait;
use reqwest::Client;
use serde_json::Value;
use tracing::{info, warn};
use crate::adapters::movie_adapter::{adapt_search_item, movie_to_search_result};
use crate::dto::request::SearchType;
use crate::dto::response::SearchResult;
use crate::error::AppError;
use crate::strategy::SearchStrategy;

const BASE_URL: &str = "https://www.omdbapi.com";

pub struct OmdbStrategy {
    client: Client,
    api_key: String,
}

impl OmdbStrategy {
    pub fn new() -> Self {
        let api_key = std::env::var("OMDB_API_KEY").unwrap_or_else(|_| {
            tracing::warn!("OMDB_API_KEY not set — movie search will return errors");
            String::from("missing_key")
        });

        OmdbStrategy {
            client: Client::builder()
                .user_agent("Mozilla/5.0 library-microservice/1.0")
                .build()
                .expect("Failed to build HTTP client"),
            api_key,
        }
    }
}

#[async_trait]
impl SearchStrategy for OmdbStrategy {
    fn search_type(&self) -> SearchType {
        SearchType::Movie
    }

    async fn search(&self, query: &str, max_results: usize) -> Result<Vec<SearchResult>, AppError> {
        info!("OMDB search: query='{}' max={}", query, max_results);

        // OMDB returns up to 10 per page; calculate pages needed
        let pages_needed = ((max_results as f64) / 10.0).ceil() as usize;
        let mut results: Vec<SearchResult> = Vec::new();

        for page in 1..=pages_needed {
            let resp = self
                .client
                .get(BASE_URL)
                .query(&[
                    ("s", query),
                    ("apikey", &self.api_key),
                    ("page", &page.to_string()),
                ])
                .send()
                .await?
                .error_for_status()?;

            let body: Value = resp.json().await?;

            // OMDB returns { "Response": "False", "Error": "..." } on failures
            if body["Response"].as_str() == Some("False") {
                let err = body["Error"].as_str().unwrap_or("OMDB error");
                warn!("OMDB search error: {}", err);
                break;
            }

            let items = body["Search"].as_array().cloned().unwrap_or_default();
            for item in &items {
                // Extract IMDb ID from search result
                if let Some(imdb_id) = item["imdbID"].as_str() {
                    info!("Found movie with imdbID: {}, fetching full details...", imdb_id);
                    // Fetch full details for this movie
                    match self.find_by_id(imdb_id).await {
                        Ok(Some(full_result)) => {
                            info!("Successfully fetched full details for {}", imdb_id);
                            results.push(full_result);
                        }
                        Ok(None) => {
                            warn!("No details found for {}, using search result", imdb_id);
                            // Fallback to basic search result if detail fetch fails
                            results.push(movie_to_search_result(adapt_search_item(item)));
                        }
                        Err(e) => {
                            warn!("Error fetching details for {}: {:?}, using search result", imdb_id, e);
                            results.push(movie_to_search_result(adapt_search_item(item)));
                        }
                    }
                } else {
                    warn!("No imdbID found in search result");
                    results.push(movie_to_search_result(adapt_search_item(item)));
                }
                if results.len() >= max_results {
                    break;
                }
            }
            if results.len() >= max_results || items.is_empty() {
                break;
            }
        }

        Ok(results)
    }

    async fn find_by_id(&self, imdb_id: &str) -> Result<Option<SearchResult>, AppError> {
        info!("OMDB detail: imdbID={}", imdb_id);

        let resp = self
            .client
            .get(BASE_URL)
            .query(&[("i", imdb_id), ("apikey", &self.api_key)])
            .send()
            .await?
            .error_for_status()?;

        let body: Value = resp.json().await?;

        if body["Response"].as_str() == Some("False") {
            return Ok(None);
        }

        let movie = adapt_search_item(&body);
        Ok(Some(movie_to_search_result(movie)))
    }
}
