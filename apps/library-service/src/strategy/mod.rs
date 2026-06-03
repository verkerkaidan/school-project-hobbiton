pub mod book_strategy;

use async_trait::async_trait;
use crate::dto::request::SearchType;
use crate::dto::response::SearchResult;
use crate::error::AppError;

/// **Strategy Pattern**
///
/// Each external API implements this trait.
/// The factory selects the right strategy based on `SearchType`.
/// The service layer calls the strategy without knowing *which* API it's talking to.
///
/// Implementations:
/// - `OpenLibraryStrategy`  → searches books via openlibrary.org
/// - `OmdbStrategy`         → searches movies via omdbapi.com
/// - `WgerStrategy`         → searches exercises via wger.de (free, no key)
#[async_trait]
pub trait SearchStrategy: Send + Sync {
    /// Which type of resource this strategy handles.
    fn search_type(&self) -> SearchType;

    /// Search for multiple results matching the query.
    async fn search(&self, query: &str, max_results: usize) -> Result<Vec<SearchResult>, AppError>;

    /// Fetch a single resource by its unique ID (ISBN, IMDb ID, exercise ID, etc.).
    async fn find_by_id(&self, id: &str) -> Result<Option<SearchResult>, AppError>;
}
