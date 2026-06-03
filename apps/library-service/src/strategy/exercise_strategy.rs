use reqwest::Client;
use async_trait::async_trait;
use serde_json::Value;
use tracing::{info, warn};
use crate::adapters::exercise_adapter::{adapt_search_item, exercise_to_search_result};
use crate::dto::request::SearchType;
use crate::dto::response::SearchResult;
use crate::error::AppError;
use crate::strategy::SearchStrategy;

const BASE_URL: &str = "https://oss.exercisedb.dev/api/v1";

pub struct ExerciseDBStrategy {
    client: Client,
}

impl ExerciseDBStrategy {
    pub fn new() -> Self {
        ExerciseDBStrategy {
            client: Client::builder()
                .user_agent("Mozilla/5.0 library-microservice/1.0")
                .build()
                .expect("Failed to build HTTP client"),
        }
    }
}

#[async_trait]
impl SearchStrategy for ExerciseDBStrategy {
    fn search_type(&self) -> SearchType {
        SearchType::Exercise
    }

    async fn search(&self, query: &str, max_results: usize) -> Result<Vec<SearchResult>, AppError> {
        let url = format!("{}/exercises/search", BASE_URL);
        info!("ExerciseDB search: query='{}' max={}", query, max_results);

        let resp = self
            .client
            .get(&url)
            .query(&[("search", query)])
            .send()
            .await?
            .error_for_status()?;

        let body: Value = resp.json().await?;

        info!("ExerciseDB response: {:?}", body);

        if body["success"].as_bool() != Some(true) {
            warn!("ExerciseDB search returned success=false for query '{}'", query);
            return Ok(vec![]);
        }

        let items = body["data"].as_array().cloned().unwrap_or_default();

        let mut results = Vec::new();

        for item in items.iter().take(max_results) {
            if let Some(exercise_id) = item["exerciseId"].as_str() {
                info!("Found exercise with ID: {}, fetching full details...", exercise_id);
                // Fetch full details for this exercise
                match self.find_by_id(exercise_id).await {
                    Ok(Some(full_result)) => {
                        info!("Successfully fetched full details for {}", exercise_id);
                        results.push(full_result);
                    }
                    Ok(None) => {
                        warn!("No details found for {}, using search result", exercise_id);
                        results.push(exercise_to_search_result(adapt_search_item(item)));
                    }
                    Err(e) => {
                        warn!("Error fetching details for {}: {:?}, using search result", exercise_id, e);
                        results.push(exercise_to_search_result(adapt_search_item(item)));
                    }
                }
            } else {
                warn!("No exerciseId found in search result");
                results.push(exercise_to_search_result(adapt_search_item(item)));
            }
        }

        Ok(results)
    }

    /// Fetch full exercise detail by exerciseId.
    /// GET /api/v1/exercises/{exerciseId}
    async fn find_by_id(&self, exercise_id: &str) -> Result<Option<SearchResult>, AppError> {
        info!("ExerciseDB detail: exerciseId={}", exercise_id);

        let resp = self
            .client
            .get(format!("{}/exercises/{}", BASE_URL, exercise_id))
            .send()
            .await?;

        // 404 = exercise doesn't exist, not an error
        if resp.status() == reqwest::StatusCode::NOT_FOUND {
            warn!("ExerciseDB: no exercise found for id '{}'", exercise_id);
            return Ok(None);
        }

        let body: Value = resp.error_for_status()?.json().await?;

        if body["success"].as_bool() != Some(true) {
            return Ok(None);
        }

        let exercise = adapt_search_item(&body["data"]);
        Ok(Some(exercise_to_search_result(exercise)))
    }
}