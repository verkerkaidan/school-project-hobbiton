use axum::{
    extract::{Path, Query, State},
    http::StatusCode,
    response::IntoResponse,
    routing::{get, post},
    Json, Router,
};
use tracing::info;

use crate::{
    dto::{
        request::{SearchQueryParams, SearchRequest, SearchType},
        response::ApiResponse,
    },
    error::AppError,
    service::LibraryService,
    AppState,
};

/// Register all library routes under `/api/v1/library`.
pub fn library_routes() -> Router<AppState> {
    Router::new()
        .route("/search", post(search_post))
        .route("/search", get(search_get))
        .route("/{type}/{id}", get(find_by_id))
        .route("/health", get(health))
}

// ─── POST /search ─────────────────────────────────────────────────────────────

/// POST endpoint for search (JSON body).
///
/// ```json
/// { "type": "BOOK", "query": "clean code", "maxResults": 10 }
/// ```
///
/// Cache key: `search:{TYPE}:{query}:{maxResults}`
async fn search_post(
    State(state): State<AppState>,
    Json(req): Json<SearchRequest>,
) -> Result<impl IntoResponse, AppError> {
    info!("POST /search type={} query='{}'", req.search_type, req.query);

    let service = LibraryService::new(state.factory, state.cache);
    let results = service.search(&req).await?;
    let total = results.len();

    Ok(Json(ApiResponse::success(
        results,
        total,
        "Search completed successfully",
    )))
}

// ─── GET /search ──────────────────────────────────────────────────────────────

/// GET endpoint for search (query params).
///
/// `/search?query=batman&type=MOVIE&maxResults=5`
///
/// Cache key: `search:{TYPE}:{query}:{maxResults}`
async fn search_get(
    State(state): State<AppState>,
    Query(params): Query<SearchQueryParams>,
) -> Result<impl IntoResponse, AppError> {
    info!(
        "GET /search type={} query='{}'",
        params.search_type, params.query
    );

    let req: SearchRequest = params.into();
    let service = LibraryService::new(state.factory, state.cache);
    let results = service.search(&req).await?;
    let total = results.len();

    Ok(Json(ApiResponse::success(
        results,
        total,
        "Search completed successfully",
    )))
}

// ─── GET /:type/:id ───────────────────────────────────────────────────────────

/// Fetch a single resource by type + ID.
///
/// Examples:
///   GET /BOOK/9780132350884     (ISBN)
///   GET /MOVIE/tt0111161        (IMDb ID)
///   GET /EXERCISE/42            (wger ID)
///
/// Cache key: `item:{TYPE}:{id}`
async fn find_by_id(
    State(state): State<AppState>,
    Path((type_str, id)): Path<(String, String)>,
) -> Result<impl IntoResponse, AppError> {
    let search_type: SearchType = type_str
        .parse()
        .map_err(|e: String| AppError::UnknownSearchType(e))?;

    info!("GET /{}/{}", search_type, id);

    let service = LibraryService::new(state.factory, state.cache);
    match service.find_by_id(search_type, &id).await? {
        Some(result) => Ok((
            StatusCode::OK,
            Json(ApiResponse::success(result, 1, "Found")),
        )),
        None => Err(AppError::NotFound(format!("No result found for id: {}", id))),
    }
}

// ─── GET /health ──────────────────────────────────────────────────────────────

async fn health() -> impl IntoResponse {
    Json(ApiResponse::success(
        "Library Microservice is running",
        1,
        "OK",
    ))
}
