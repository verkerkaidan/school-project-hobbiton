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
