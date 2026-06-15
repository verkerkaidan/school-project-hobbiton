use serde_json::Value;
use crate::dto::response::{ExerciseResponse, SearchResult};
use crate::models::exercise::Exercise;
use tracing::debug;
use serde_json::json;

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


#[test]
fn maps_all_fields_from_full_detail_response() {
    let raw = json!({
        "exerciseId":      "0001",
        "name":            "Barbell Squat",
        "gifUrl":          "https://example.com/squat.gif",
        "targetMuscles":   ["quadriceps", "hamstrings"],
        "bodyParts":       ["upper legs"],
        "equipments":      ["barbell"],
        "secondaryMuscles":["glutes", "calves"],
        "instructions":    ["Stand with feet shoulder-width apart.", "Lower until thighs parallel."]
    });

    let ex = adapt_search_item(&raw);

    assert_eq!(ex.exercise_id,       Some("0001".into()));
    assert_eq!(ex.name,              Some("Barbell Squat".into()));
    assert_eq!(ex.gif_url,           Some("https://example.com/squat.gif".into()));
    assert_eq!(ex.target_muscles,    Some(vec!["quadriceps".into(), "hamstrings".into()]));
    assert_eq!(ex.body_parts,        Some(vec!["upper legs".into()]));
    assert_eq!(ex.equipments,        Some(vec!["barbell".into()]));
    assert_eq!(ex.secondary_muscles, Some(vec!["glutes".into(), "calves".into()]));
    assert_eq!(
        ex.instructions,
        Some(vec![
            "Stand with feet shoulder-width apart.".into(),
            "Lower until thighs parallel.".into(),
        ])
    );
}

#[test]
fn search_result_shape_has_only_id_name_and_gif() {
    // The /exercises/search endpoint only returns these three fields.
    let raw = json!({
        "exerciseId": "0042",
        "name":       "Push-up",
        "gifUrl":     "https://example.com/pushup.gif"
    });

    let ex = adapt_search_item(&raw);

    assert_eq!(ex.exercise_id, Some("0042".into()));
    assert_eq!(ex.name,        Some("Push-up".into()));
    assert_eq!(ex.gif_url,     Some("https://example.com/pushup.gif".into()));
    // Arrays absent from the JSON become None (not empty vec)
    assert_eq!(ex.target_muscles,    None);
    assert_eq!(ex.body_parts,        None);
    assert_eq!(ex.equipments,        None);
    assert_eq!(ex.secondary_muscles, None);
    assert_eq!(ex.instructions,      None);
}

#[test]
fn empty_json_produces_all_none() {
    let ex = adapt_search_item(&json!({}));

    assert_eq!(ex.exercise_id,       None);
    assert_eq!(ex.name,              None);
    assert_eq!(ex.gif_url,           None);
    assert_eq!(ex.target_muscles,    None);
    assert_eq!(ex.body_parts,        None);
    assert_eq!(ex.equipments,        None);
    assert_eq!(ex.secondary_muscles, None);
    assert_eq!(ex.instructions,      None);
}

#[test]
fn empty_arrays_become_some_empty_vec() {
    // An explicit empty array in JSON should produce Some([]), not None.
    let raw = json!({
        "exerciseId":    "1",
        "targetMuscles": [],
        "instructions":  []
    });

    let ex = adapt_search_item(&raw);

    assert_eq!(ex.target_muscles, Some(vec![]));
    assert_eq!(ex.instructions,   Some(vec![]));
}

#[test]
fn non_string_entries_in_arrays_are_filtered_out() {
    // Robustness: if OMDB/ExerciseDB returns mixed arrays, only strings survive.
    let raw = json!({
        "exerciseId":    "2",
        "targetMuscles": ["biceps", 42, null, "triceps"]
    });

    let ex = adapt_search_item(&raw);

    // 42 and null should be silently dropped
    assert_eq!(ex.target_muscles, Some(vec!["biceps".into(), "triceps".into()]));
}

// ─── exercise_to_search_result ───────────────────────────────────────────────

#[test]
fn wraps_as_exercise_variant() {
    let raw = json!({ "exerciseId": "99", "name": "Lunge" });
    let result = exercise_to_search_result(adapt_search_item(&raw));
    assert!(matches!(result, SearchResult::Exercise(_)));
}

#[test]
fn id_propagated_through_conversion() {
    let raw = json!({ "exerciseId": "77" });
    let result = exercise_to_search_result(adapt_search_item(&raw));
    assert_eq!(result.id(), Some("77".into()));
}

#[test]
fn all_fields_propagated_to_exercise_response() {
    let raw = json!({
        "exerciseId":      "55",
        "name":            "Deadlift",
        "gifUrl":          "https://example.com/dl.gif",
        "targetMuscles":   ["lower back", "hamstrings"],
        "bodyParts":       ["waist"],
        "equipments":      ["barbell"],
        "secondaryMuscles":["forearms"],
        "instructions":    ["Step 1", "Step 2"]
    });

    let result = exercise_to_search_result(adapt_search_item(&raw));

    if let SearchResult::Exercise(e) = result {
        assert_eq!(e.exercise_id,      Some("55".into()));
        assert_eq!(e.name,             Some("Deadlift".into()));
        assert_eq!(e.gif_url,          Some("https://example.com/dl.gif".into()));
        assert_eq!(e.target_muscles,   Some(vec!["lower back".into(), "hamstrings".into()]));
        assert_eq!(e.body_parts,       Some(vec!["waist".into()]));
        assert_eq!(e.equipments,       Some(vec!["barbell".into()]));
        assert_eq!(e.secondary_muscles,Some(vec!["forearms".into()]));
        assert_eq!(e.instructions,     Some(vec!["Step 1".into(), "Step 2".into()]));
    } else {
        panic!("expected Exercise variant");
    }
}