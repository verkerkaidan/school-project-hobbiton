use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use serde_with::skip_serializing_none;

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

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase", tag = "resultType")]
#[derive(Debug)]
pub enum SearchResult {
    Book(BookResponse),
    Exercise(ExerciseResponse),
    Movie(MovieResponse)
}

#[derive(Serialize, Deserialize, Clone, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct BookResponse {
    pub id: Option<String>,
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

#[derive(Serialize, Deserialize, Clone, Debug, Default)]
#[serde(rename_all = "camelCase")]
#[skip_serializing_none]
pub struct MovieResponse {
    pub id: Option<String>,
    pub title: Option<String>,
    pub year: Option<String>,
    pub rated: Option<String>,
    pub released: Option<String>,
    pub runtime: Option<String>,
    pub genre: Option<String>,
    pub director: Option<String>,
    pub writer: Option<String>,
    pub actors: Option<String>,
    pub plot: Option<String>,
    pub language: Option<String>,
    pub country: Option<String>,
    pub awards: Option<String>,
    pub poster: Option<String>,
    pub imdb_rating: Option<String>,
    pub media_type: Option<String>,
}

#[derive(Serialize, Deserialize, Clone, Debug, Default)]
#[serde(rename_all = "camelCase")]
#[skip_serializing_none]
pub struct ExerciseResponse {
    pub exercise_id: Option<String>,
    pub name: Option<String>,
    pub gif_url: Option<String>,
    pub target_muscles: Option<Vec<String>>,
    pub body_parts: Option<Vec<String>>,
    pub equipments: Option<Vec<String>>,
    pub secondary_muscles: Option<Vec<String>>,
    pub instructions: Option<Vec<String>>,
}

impl SearchResult {
    pub fn id(&self) -> Option<String> {
        match self {
            SearchResult::Book(b) => b.id.clone(),
            SearchResult::Exercise(e) => e.exercise_id.clone(),
            SearchResult::Movie(m) => m.id.clone(),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn book_result_returns_book_id() {
        let result = SearchResult::Book(BookResponse {
            id: Some("book123".into()),
            title: None,
            authors: vec![],
            published_date: None,
            publishers: vec![],
            isbn: vec![],
            page_count: None,
            languages: vec![],
            thumbnail_url: None,
            cover_id: None,
        });

        assert_eq!(result.id(), Some("book123".to_string()));
    }

    #[test]
    fn movie_result_returns_movie_id() {
        let result = SearchResult::Movie(MovieResponse {
            id: Some("tt0111161".into()),
            ..Default::default()
        });

        assert_eq!(result.id(), Some("tt0111161".to_string()));
    }

    #[test]
    fn exercise_result_returns_exercise_id() {
        let result = SearchResult::Exercise(ExerciseResponse {
            exercise_id: Some("42".into()),
            ..Default::default()
        });

        assert_eq!(result.id(), Some("42".to_string()));
    }

    #[test]
    fn api_response_success_sets_fields() {
        let response = ApiResponse::success(vec!["test"], 1, "Success");

        assert!(response.success);
        assert_eq!(response.message, "Success");
        assert_eq!(response.total_results, 1);
        assert!(response.data.is_some());
    }

    #[test]
    fn api_response_error_sets_fields() {
        let response: ApiResponse<String> = ApiResponse::error("Something failed");

        assert!(!response.success);
        assert_eq!(response.message, "Something failed");
        assert_eq!(response.total_results, 0);
        assert!(response.data.is_none());
    }
}