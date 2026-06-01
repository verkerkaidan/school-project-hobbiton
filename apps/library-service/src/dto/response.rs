use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ApiResponse<T: Serialize> {
    pub success: bool,
    pub data: Option<T>,
    pub message: String,
    pub timestamp: DateTime<Utc>,
    pub total_results: usize
}

impl<T: Serialize> ApiResponse<T> {
    pub fn success(data: T, total_results: usize, message: impl Into<String>) -> Self {
        ApiResponse {
            message: message.into(),
            success: true,
            data: Some(data),
            timestamp: Utc::now(),
            total_results
        }
    }

    #[allow(dead_code)]
    pub fn error(message: impl Into<String>) -> Self {
        ApiResponse {
            success: false,
            data: None,
            message: message.into(),
            timestamp: Utc::now(),
            total_results: 0
        }
    }
}

#[derive(Serialize, Deserialize, Clone, Debug)]
#[serde(rename_all = "camelCase", tag = "resultType")]
pub enum SearchResult {
    Book(BookResponse),
}

#[derive(Serialize, Deserialize, Clone, Debug)]
#[serde(rename_all = "camelCase")]
pub struct BookResponse {
    pub title: Option<String>,
    pub authors: Vec<String>,
    pub published_date: Option<String>,
    pub publishers: Vec<String>,
    pub isbn: Vec<String>,
    pub page_count: Option<u32>,
    pub languages: Vec<String>,
    pub thumbnail_url: Option<String>,
    pub cover_id: Option<String>,
}