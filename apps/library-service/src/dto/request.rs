use serde::{Deserialize, Serialize};

/// Supported search types — mirrors the Java SearchType enum.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "UPPERCASE")]
pub enum SearchType {
    Book,
    Movie,
    Exercise,
}

impl std::fmt::Display for SearchType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            SearchType::Book => write!(f, "BOOK"),
            SearchType::Movie => write!(f, "MOVIE"),
            SearchType::Exercise => write!(f, "EXERCISE"),
        }
    }
}

impl std::str::FromStr for SearchType {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_uppercase().as_str() {
            "BOOK" => Ok(SearchType::Book),
            "MOVIE" => Ok(SearchType::Movie),
            "EXERCISE" => Ok(SearchType::Exercise),
            other => Err(format!("Unknown search type: {}", other)),
        }
    }
}

/// Unified search request — used for both POST (body) and GET (query params).
///
/// POST body example:
/// ```json
/// { "type": "BOOK", "query": "clean code", "maxResults": 10 }
/// ```
///
/// GET example:
/// /search?query=clean+code&type=BOOK&maxResults=10
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SearchRequest {
    #[serde(rename = "type")]
    pub search_type: SearchType,
    pub query: String,
    #[serde(default = "default_max_results")]
    pub max_results: usize,
}

fn default_max_results() -> usize {
    10
}

/// Query params version of SearchRequest (for GET endpoints).
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SearchQueryParams {
    pub query: String,
    #[serde(rename = "type")]
    pub search_type: SearchType,
    #[serde(default = "default_max_results")]
    pub max_results: usize,
}

impl From<SearchQueryParams> for SearchRequest {
    fn from(p: SearchQueryParams) -> Self {
        SearchRequest {
            search_type: p.search_type,
            query: p.query,
            max_results: p.max_results,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::str::FromStr;

    #[test]
    fn display_outputs_uppercase_values() {
        assert_eq!(SearchType::Book.to_string(), "BOOK");
        assert_eq!(SearchType::Movie.to_string(), "MOVIE");
        assert_eq!(SearchType::Exercise.to_string(), "EXERCISE");
    }

    #[test]
    fn from_str_is_case_insensitive() {
        assert_eq!(
            SearchType::from_str("book").unwrap(),
            SearchType::Book
        );

        assert_eq!(
            SearchType::from_str("MOVIE").unwrap(),
            SearchType::Movie
        );

        assert_eq!(
            SearchType::from_str("Exercise").unwrap(),
            SearchType::Exercise
        );
    }

    #[test]
    fn from_str_returns_error_for_unknown_type() {
        assert!(SearchType::from_str("foobar").is_err());
    }

    #[test]
    fn query_params_convert_to_request() {
        let params = SearchQueryParams {
            query: "clean code".to_string(),
            search_type: SearchType::Book,
            max_results: 25,
        };

        let request: SearchRequest = params.into();

        assert_eq!(request.query, "clean code");
        assert_eq!(request.search_type, SearchType::Book);
        assert_eq!(request.max_results, 25);
    }

    #[test]
    fn default_max_results_is_ten() {
        assert_eq!(default_max_results(), 10);
    }
}
