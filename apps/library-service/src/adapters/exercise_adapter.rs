use serde_json::Value;
use crate::dto::response::{ExerciseResponse, SearchResult};
use crate::models::exercise::Exercise;
use tracing::debug;

/// Adapt a single search result item from the `/exercises/search` endpoint.
/// Search results only return exerciseId, name and gifUrl — full data
/// comes from `find_by_id` if the user drills in.
pub fn adapt_search_item(raw: &Value) -> Exercise {
    debug!("Adapting ExerciseDB response: {}", raw);

    Exercise {
        exercise_id: opt_str(raw, "exerciseId"),
        name: opt_str(raw, "name"),
        gif_url: opt_str(raw, "gifUrl"),
        target_muscles: opt_vec_str(raw, "targetMuscles"),
        body_parts: opt_vec_str(raw, "bodyParts"),
        equipments: opt_vec_str(raw, "equipments"),
        secondary_muscles: opt_vec_str(raw, "secondaryMuscles"),
        instructions: opt_vec_str(raw, "instructions"),
    }
}

/// Convert internal `Exercise` → `SearchResult::Exercise(ExerciseResponse)`.
pub fn exercise_to_search_result(ex: Exercise) -> SearchResult {
    SearchResult::Exercise(ExerciseResponse {
        exercise_id: ex.exercise_id,
        name: ex.name,
        gif_url: ex.gif_url,
        target_muscles: ex.target_muscles,
        body_parts: ex.body_parts,
        equipments: ex.equipments,
        secondary_muscles: ex.secondary_muscles,
        instructions: ex.instructions,
    })
}

// helpers

fn opt_str(val: &Value, key: &str) -> Option<String> {
    val[key].as_str().map(str::to_owned)
}

fn opt_vec_str(val: &Value, key: &str) -> Option<Vec<String>> {
    val[key].as_array().map(|arr| {
        arr.iter()
            .filter_map(|v| v.as_str().map(str::to_owned))
            .collect()
    })
}