use std::collections::HashMap;
use std::sync::Arc;

use crate::dto::request::SearchType;
use crate::error::AppError;
use crate::strategy::book_strategy::OpenLibraryStrategy;
use crate::strategy::exercise_strategy::ExerciseDBStrategy;
use crate::strategy::movie_strategy::OmdbStrategy;
use crate::strategy::SearchStrategy;

/// **Factory Pattern**
///
/// `ServiceFactory` owns all `SearchStrategy` implementations.
/// On startup it registers every strategy in a HashMap keyed by `SearchType`.
///
/// The controller calls `factory.get(search_type)` and gets back the right
/// strategy — no if/else chains needed anywhere.
///
/// Adding a new API = implement `SearchStrategy` + register here.
pub struct ServiceFactory {
    strategies: HashMap<SearchType, Arc<dyn SearchStrategy>>,
}

impl ServiceFactory {
    /// Build the factory and register all strategies.
    pub fn new() -> Self {
        let mut strategies: HashMap<SearchType, Arc<dyn SearchStrategy>> = HashMap::new();

        strategies.insert(
            SearchType::Book,
            Arc::new(OpenLibraryStrategy::new()),
        );

        strategies.insert(
            SearchType::Exercise,
            Arc::new(ExerciseDBStrategy::new()),
        );

        strategies.insert(
            SearchType::Movie,
            Arc::new(OmdbStrategy::new()),
        );

        ServiceFactory { strategies }
    }
    pub fn new_with(strategies: HashMap<SearchType, Arc<dyn SearchStrategy>>) -> Self {
        Self { strategies }
    }

    /// Resolve the strategy for the given `SearchType`.
    ///
    /// Returns `AppError::UnknownSearchType` if no strategy is registered,
    /// which results in a 400 Bad Request response.
    pub fn get(&self, search_type: SearchType) -> Result<Arc<dyn SearchStrategy>, AppError> {
        self.strategies
            .get(&search_type)
            .cloned()
            .ok_or_else(|| AppError::UnknownSearchType(search_type.to_string()))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn returns_book_strategy() {
        let factory = ServiceFactory::new();

        let strategy =
            factory.get(SearchType::Book).unwrap();

        assert_eq!(
            strategy.search_type(),
            SearchType::Book
        );
    }

    #[test]
    fn returns_movie_strategy() {
        let factory = ServiceFactory::new();

        let strategy =
            factory.get(SearchType::Movie).unwrap();

        assert_eq!(
            strategy.search_type(),
            SearchType::Movie
        );
    }

    #[test]
    fn returns_exercise_strategy() {
        let factory = ServiceFactory::new();

        let strategy =
            factory.get(SearchType::Exercise).unwrap();

        assert_eq!(
            strategy.search_type(),
            SearchType::Exercise
        );
    }
}
