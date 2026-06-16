// src/routes/mod.rs

use axum::{
    extract::{Path, Query, State},
    http::StatusCode,
    response::IntoResponse,
    routing::{delete, get, post, put},
    Json, Router,
};
use serde::Deserialize;
use tracing::info;
use uuid::Uuid;

use crate::{
    db::{List, ListItem},
    dto::{
        request::{SearchQueryParams, SearchRequest, SearchType},
        response::{ApiResponse, SearchResult},
    },
    error::AppError,
    service::LibraryService,
    AppState,
};

pub fn library_routes() -> Router<AppState> {
    Router::new()
        // ── Search ──────────────────────────────────────────────────────────
        .route("/search", post(search_post).get(search_get))
        .route("/health", get(health))
        // ── Single item lookup ───────────────────────────────────────────────
        .route("/{type}/{id}", get(find_by_id))
        // ── Lists ────────────────────────────────────────────────────────────
        .route("/users/{user_id}/lists", get(get_lists).post(create_list))
        .route("/users/{user_id}/lists/{list_id}", get(get_list).put(rename_list).delete(delete_list))
        // ── List items ───────────────────────────────────────────────────────
        .route("/lists/{list_id}/items", get(get_items).post(add_item))
        .route("/lists/{list_id}/items/resolved", get(get_resolved_items)) // before {item_id}
        .route("/lists/{list_id}/items/{item_id}", delete(remove_item).put(update_item_notes))
        .route("/lists/{list_id}/items/reorder", post(reorder_items))
        // ── Convenience ──────────────────────────────────────────────────────
        .route("/lists/{list_id}/search-and-add", post(search_and_add))
        .route("/ping", get(|| async { "pong" }))
}

// ─── Request bodies ───────────────────────────────────────────────────────────

#[derive(Deserialize)]
struct CreateListBody {
    name: String,
    description: Option<String>,
}

#[derive(Deserialize)]
struct RenameListBody {
    name: Option<String>,
    description: Option<String>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct AddItemBody {
    item_type: String,
    item_id: String,
    notes: Option<String>,
}

#[derive(Deserialize)]
struct UpdateNotesBody {
    notes: Option<String>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct ReorderBody {
    /// List of item IDs in the new desired order
    item_ids: Vec<String>,
}

/// Search for an item and immediately add the first result to a list.
/// Useful for "add to list" flows from a search UI.
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct SearchAndAddBody {
    item_type: String,
    /// If item_id is provided, skip search and add directly by ID
    item_id: Option<String>,
    /// Used when item_id is not provided — searches and picks the first result
    query: Option<String>,
    notes: Option<String>,
}

// ─── Search handlers ──────────────────────────────────────────────────────────

async fn search_post(
    State(state): State<AppState>,
    Json(req): Json<SearchRequest>,
) -> Result<impl IntoResponse, AppError> {
    info!("POST /search type={} query='{}'", req.search_type, req.query);
    let service = LibraryService::new(state.factory, state.cache);
    let results = service.search(&req).await?;
    let total = results.len();
    Ok(Json(ApiResponse::success(results, total, "Search completed successfully")))
}

async fn search_get(
    State(state): State<AppState>,
    Query(params): Query<SearchQueryParams>,
) -> Result<impl IntoResponse, AppError> {
    info!("GET /search type={} query='{}'", params.search_type, params.query);
    let req: SearchRequest = params.into();
    let service = LibraryService::new(state.factory, state.cache);
    let results = service.search(&req).await?;
    let total = results.len();
    Ok(Json(ApiResponse::success(results, total, "Search completed successfully")))
}

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
        Some(result) => Ok((StatusCode::OK, Json(ApiResponse::success(result, 1, "Found")))),
        None => Err(AppError::NotFound(format!("No result found for id: {}", id))),
    }
}

async fn health() -> impl IntoResponse {
    Json(ApiResponse::success("Library Microservice is running", 1, "OK"))
}

// ─── List handlers ────────────────────────────────────────────────────────────

/// GET /users/:user_id/lists — all lists for a user
async fn get_lists(
    State(s): State<AppState>,
    Path(user_id): Path<String>,
) -> Result<Json<ApiResponse<Vec<List>>>, AppError> {
    let lists = s.db.get_lists_for_user(&user_id).await?;
    let n = lists.len();
    Ok(Json(ApiResponse::success(lists, n, "OK")))
}

/// GET /users/:user_id/lists/:list_id — single list metadata
async fn get_list(
    State(s): State<AppState>,
    Path((user_id, list_id)): Path<(String, Uuid)>,
) -> Result<Json<ApiResponse<List>>, AppError> {
    let list = s.db.get_list(list_id, &user_id).await?
        .ok_or_else(|| AppError::NotFound(format!("List {} not found", list_id)))?;
    Ok(Json(ApiResponse::success(list, 1, "OK")))
}

/// POST /users/:user_id/lists — create a new list
async fn create_list(
    State(s): State<AppState>,
    Path(user_id): Path<String>,
    Json(body): Json<CreateListBody>,
) -> Result<Json<ApiResponse<List>>, AppError> {
    if body.name.trim().is_empty() {
        return Err(AppError::UnknownSearchType("List name cannot be empty".into()));
    }
    let list = s.db.create_list(&user_id, body.name.trim(), body.description.as_deref()).await?;
    Ok(Json(ApiResponse::success(list, 1, "Created")))
}

/// PUT /users/:user_id/lists/:list_id — rename or update description
async fn rename_list(
    State(s): State<AppState>,
    Path((user_id, list_id)): Path<(String, Uuid)>,
    Json(body): Json<RenameListBody>,
) -> Result<Json<ApiResponse<List>>, AppError> {
    let list = s.db.update_list(list_id, &user_id, body.name.as_deref(), body.description.as_deref()).await?
        .ok_or_else(|| AppError::NotFound(format!("List {} not found", list_id)))?;
    Ok(Json(ApiResponse::success(list, 1, "Updated")))
}

/// DELETE /users/:user_id/lists/:list_id
async fn delete_list(
    State(s): State<AppState>,
    Path((user_id, list_id)): Path<(String, Uuid)>,
) -> Result<Json<ApiResponse<bool>>, AppError> {
    let deleted = s.db.delete_list(list_id, &user_id).await?;
    if !deleted {
        return Err(AppError::NotFound(format!("List {} not found", list_id)));
    }
    Ok(Json(ApiResponse::success(true, 1, "Deleted")))
}

// ─── Item handlers ────────────────────────────────────────────────────────────

/// GET /lists/:list_id/items — raw list items (IDs only, no hydration)
async fn get_items(
    State(s): State<AppState>,
    Path(list_id): Path<Uuid>,
) -> Result<Json<ApiResponse<Vec<ListItem>>>, AppError> {
    let items = s.db.get_items(list_id).await?;
    let n = items.len();
    Ok(Json(ApiResponse::success(items, n, "OK")))
}

/// GET /lists/:list_id/items/resolved — items hydrated with full API data
/// Books get covers, movies get posters, exercises get GIFs — all via cache
async fn get_resolved_items(
    State(s): State<AppState>,
    Path(list_id): Path<Uuid>,
) -> Result<Json<ApiResponse<Vec<SearchResult>>>, AppError> {
    let items = s.db.get_items(list_id).await?;
    if items.is_empty() {
        return Ok(Json(ApiResponse::success(vec![], 0, "OK")));
    }

    let service = LibraryService::new(s.factory.clone(), s.cache.clone());
    let mut resolved = Vec::with_capacity(items.len());

    for item in &items {
        let search_type: SearchType = item
            .item_type
            .parse()
            .map_err(|_| AppError::UnknownSearchType(item.item_type.clone()))?;

        match service.find_by_id(search_type, &item.item_id).await? {
            Some(result) => resolved.push(result),
            None => tracing::warn!(
                "item_id={} type={} not found in external API, skipping",
                item.item_id, item.item_type
            ),
        }
    }

    let n = resolved.len();
    Ok(Json(ApiResponse::success(resolved, n, "OK")))
}

/// POST /lists/:list_id/items — add an item to a list by known ID
async fn add_item(
    State(s): State<AppState>,
    Path(list_id): Path<Uuid>,
    Json(body): Json<AddItemBody>,
) -> Result<Json<ApiResponse<ListItem>>, AppError> {
    let item_type = body.item_type.to_uppercase();
    item_type
        .parse::<SearchType>()
        .map_err(|_| AppError::UnknownSearchType(item_type.clone()))?;

    // Verify the item actually exists before adding it to the list
    let service = LibraryService::new(s.factory.clone(), s.cache.clone());
    let search_type: SearchType = item_type.parse().unwrap();
    match service.find_by_id(search_type, &body.item_id).await? {
        None => return Err(AppError::NotFound(format!(
            "{} with id '{}' not found", item_type, body.item_id
        ))),
        Some(_) => {}
    }

    let item = s.db.add_item(list_id, &item_type, &body.item_id, body.notes.as_deref()).await?;
    Ok(Json(ApiResponse::success(item, 1, "Added")))
}

/// DELETE /lists/:list_id/items/:item_id
async fn remove_item(
    State(s): State<AppState>,
    Path((list_id, item_id)): Path<(Uuid, String)>,
) -> Result<Json<ApiResponse<bool>>, AppError> {
    let removed = s.db.remove_item(list_id, &item_id).await?;
    if !removed {
        return Err(AppError::NotFound(format!("Item {} not in list {}", item_id, list_id)));
    }
    Ok(Json(ApiResponse::success(true, 1, "Removed")))
}

/// PUT /lists/:list_id/items/:item_id — update notes on a saved item
async fn update_item_notes(
    State(s): State<AppState>,
    Path((list_id, item_id)): Path<(Uuid, String)>,
    Json(body): Json<UpdateNotesBody>,
) -> Result<Json<ApiResponse<ListItem>>, AppError> {
    let item = s.db.update_item_notes(list_id, &item_id, body.notes.as_deref()).await?
        .ok_or_else(|| AppError::NotFound(format!("Item {} not in list {}", item_id, list_id)))?;
    Ok(Json(ApiResponse::success(item, 1, "Updated")))
}

/// POST /lists/:list_id/items/reorder — set new positions for all items
async fn reorder_items(
    State(s): State<AppState>,
    Path(list_id): Path<Uuid>,
    Json(body): Json<ReorderBody>,
) -> Result<Json<ApiResponse<Vec<ListItem>>>, AppError> {
    if body.item_ids.is_empty() {
        return Err(AppError::UnknownSearchType("item_ids cannot be empty".into()));
    }
    let items = s.db.reorder_items(list_id, &body.item_ids).await?;
    let n = items.len();
    Ok(Json(ApiResponse::success(items, n, "Reordered")))
}

/// POST /lists/:list_id/search-and-add
/// Either adds by known item_id, or searches and picks the first result.
/// Great for "add to list" buttons in the UI.
async fn search_and_add(
    State(s): State<AppState>,
    Path(list_id): Path<Uuid>,
    Json(body): Json<SearchAndAddBody>,
) -> Result<Json<ApiResponse<ListItem>>, AppError> {
    let item_type = body.item_type.to_uppercase();
    let search_type: SearchType = item_type
        .parse()
        .map_err(|_| AppError::UnknownSearchType(item_type.clone()))?;

    let service = LibraryService::new(s.factory.clone(), s.cache.clone());

    let item_id = if let Some(id) = body.item_id {
        // Direct add by ID — verify it exists
        match service.find_by_id(search_type, &id).await? {
            None => return Err(AppError::NotFound(format!(
                "{} with id '{}' not found", item_type, id
            ))),
            Some(_) => id,
        }
    } else if let Some(query) = body.query {
        // Search and pick first result
        let req = SearchRequest {
            search_type,
            query: query.clone(),
            max_results: 1,
        };
        let results = service.search(&req).await?;
        let first = results.into_iter().next()
            .ok_or_else(|| AppError::NotFound(format!(
                "No {} found for query '{}'", item_type, query
            )))?;
        first.id().ok_or_else(|| AppError::Internal(
            anyhow::anyhow!("Search result had no ID")
        ))?
    } else {
        return Err(AppError::UnknownSearchType(
            "Either item_id or query must be provided".into()
        ));
    };

    let item = s.db.add_item(list_id, &item_type, &item_id, body.notes.as_deref()).await?;
    Ok(Json(ApiResponse::success(item, 1, "Added")))
}