use serde_json::Value;
use crate::dto::response::{MovieResponse, SearchResult};
use crate::models::movie::Movie;
use tracing::debug;
use serde_json::json;


pub fn adapt_search_item(raw: &Value) -> Movie {
    debug!("Adapting OMDB response: {}", raw);

    Movie {
        id: opt_str(raw, "imdbID"),
        title: opt_str(raw, "Title"),
        year: opt_str(raw, "Year"),
        rated: opt_str(raw, "Rated").filter(|u| u != "N/A"),
        released: opt_str(raw, "Released").filter(|u| u != "N/A"),
        runtime: opt_str(raw, "Runtime").filter(|u| u != "N/A"),
        genre: opt_str(raw, "Genre").filter(|u| u != "N/A"),
        director: opt_str(raw, "Director").filter(|u| u != "N/A"),
        writer: opt_str(raw, "Writer").filter(|u| u != "N/A"),
        actors: opt_str(raw, "Actors").filter(|u| u != "N/A"),
        plot: opt_str(raw, "Plot").filter(|u| u != "N/A"),
        language: opt_str(raw, "Language").filter(|u| u != "N/A"),
        country: opt_str(raw, "Country").filter(|u| u != "N/A"),
        awards: opt_str(raw, "Awards").filter(|u| u != "N/A"),
        poster: opt_str(raw, "Poster").filter(|u| u != "N/A"),
        imdb_rating: opt_str(raw, "imdbRating").filter(|u| u != "N/A"),
        media_type: opt_str(raw, "Type"),
    }
}

pub fn movie_to_search_result(movie: Movie) -> SearchResult {
    SearchResult::Movie(MovieResponse {
        id: movie.id,
        title: movie.title,
        year: movie.year,
        rated: movie.rated,
        released: movie.released,
        runtime: movie.runtime,
        genre: movie.genre,
        director: movie.director,
        writer: movie.writer,
        actors: movie.actors,
        plot: movie.plot,
        language: movie.language,
        country: movie.country,
        awards: movie.awards,
        poster: movie.poster,
        imdb_rating: movie.imdb_rating,
        media_type: movie.media_type,
    })
}




//helper
fn opt_str(val: &Value, key: &str) -> Option<String> {
    val[key].as_str().map(str::to_owned)
}


#[test]
fn maps_all_standard_fields() {
    let raw = json!({
        "imdbID":    "tt0111161",
        "Title":     "The Shawshank Redemption",
        "Year":      "1994",
        "Rated":     "R",
        "Released":  "14 Oct 1994",
        "Runtime":   "142 min",
        "Genre":     "Drama",
        "Director":  "Frank Darabont",
        "Writer":    "Stephen King",
        "Actors":    "Tim Robbins, Morgan Freeman",
        "Plot":      "Two imprisoned men bond.",
        "Language":  "English",
        "Country":   "United States",
        "Awards":    "Nominated for 7 Oscars",
        "Poster":    "https://example.com/poster.jpg",
        "imdbRating": "9.3",
        "Type":      "movie"
    });

    let movie = adapt_search_item(&raw);

    assert_eq!(movie.id,          Some("tt0111161".into()));
    assert_eq!(movie.title,       Some("The Shawshank Redemption".into()));
    assert_eq!(movie.year,        Some("1994".into()));
    assert_eq!(movie.rated,       Some("R".into()));
    assert_eq!(movie.released,    Some("14 Oct 1994".into()));
    assert_eq!(movie.runtime,     Some("142 min".into()));
    assert_eq!(movie.genre,       Some("Drama".into()));
    assert_eq!(movie.director,    Some("Frank Darabont".into()));
    assert_eq!(movie.writer,      Some("Stephen King".into()));
    assert_eq!(movie.actors,      Some("Tim Robbins, Morgan Freeman".into()));
    assert_eq!(movie.plot,        Some("Two imprisoned men bond.".into()));
    assert_eq!(movie.language,    Some("English".into()));
    assert_eq!(movie.country,     Some("United States".into()));
    assert_eq!(movie.awards,      Some("Nominated for 7 Oscars".into()));
    assert_eq!(movie.poster,      Some("https://example.com/poster.jpg".into()));
    assert_eq!(movie.imdb_rating, Some("9.3".into()));
    assert_eq!(movie.media_type,  Some("movie".into()));
}

#[test]
fn na_values_are_filtered_to_none() {
    // OMDB uses the literal string "N/A" for unknown fields.
    // Our adapter must convert those to None.
    let raw = json!({
        "imdbID":    "tt9999999",
        "Title":     "Some Film",
        "Rated":     "N/A",
        "Released":  "N/A",
        "Runtime":   "N/A",
        "Genre":     "N/A",
        "Director":  "N/A",
        "Writer":    "N/A",
        "Actors":    "N/A",
        "Plot":      "N/A",
        "Language":  "N/A",
        "Country":   "N/A",
        "Awards":    "N/A",
        "Poster":    "N/A",
        "imdbRating": "N/A"
    });

    let movie = adapt_search_item(&raw);

    assert_eq!(movie.rated,       None, "rated N/A must become None");
    assert_eq!(movie.released,    None);
    assert_eq!(movie.runtime,     None);
    assert_eq!(movie.genre,       None);
    assert_eq!(movie.director,    None);
    assert_eq!(movie.writer,      None);
    assert_eq!(movie.actors,      None);
    assert_eq!(movie.plot,        None);
    assert_eq!(movie.language,    None);
    assert_eq!(movie.country,     None);
    assert_eq!(movie.awards,      None);
    assert_eq!(movie.poster,      None);
    assert_eq!(movie.imdb_rating, None);
}

#[test]
fn empty_json_produces_all_none_fields() {
    let movie = adapt_search_item(&json!({}));

    assert_eq!(movie.id,    None);
    assert_eq!(movie.title, None);
    assert_eq!(movie.year,  None);
}

#[test]
fn year_is_not_na_filtered() {
    // Year is never "N/A" in practice but is not filtered anyway
    // (the adapter has no filter on "Year").
    let raw = json!({ "Year": "2024" });
    let movie = adapt_search_item(&raw);
    assert_eq!(movie.year, Some("2024".into()));
}

// ─── movie_to_search_result ──────────────────────────────────────────────────

#[test]
fn wraps_as_movie_variant() {
    let raw = json!({ "imdbID": "tt0000001", "Title": "Test Film", "Type": "movie" });
    let result = movie_to_search_result(adapt_search_item(&raw));
    assert!(matches!(result, SearchResult::Movie(_)));
}

#[test]
fn id_propagated_through_conversion() {
    let raw = json!({ "imdbID": "tt0111161" });
    let result = movie_to_search_result(adapt_search_item(&raw));
    assert_eq!(result.id(), Some("tt0111161".into()));
}

#[test]
fn na_fields_absent_from_movie_response() {
    let raw = json!({
        "imdbID": "tt1",
        "Title": "Film",
        "Rated": "N/A",
        "Plot": "N/A"
    });
    let result = movie_to_search_result(adapt_search_item(&raw));
    if let SearchResult::Movie(m) = result {
        assert_eq!(m.rated, None);
        assert_eq!(m.plot,  None);
    } else {
        panic!("expected Movie variant");
    }
}

#[test]
fn all_fields_propagated_to_movie_response() {
    let raw = json!({
        "imdbID": "tt0000001",
        "Title": "Propagation Test",
        "Year": "2000",
        "Rated": "PG",
        "Genre": "Action",
        "Director": "Director Name",
        "imdbRating": "7.5",
        "Type": "movie"
    });

    let result = movie_to_search_result(adapt_search_item(&raw));
    if let SearchResult::Movie(m) = result {
        assert_eq!(m.id,          Some("tt0000001".into()));
        assert_eq!(m.title,       Some("Propagation Test".into()));
        assert_eq!(m.year,        Some("2000".into()));
        assert_eq!(m.rated,       Some("PG".into()));
        assert_eq!(m.genre,       Some("Action".into()));
        assert_eq!(m.director,    Some("Director Name".into()));
        assert_eq!(m.imdb_rating, Some("7.5".into()));
        assert_eq!(m.media_type,  Some("movie".into()));
    } else {
        panic!("expected Movie variant");
    }
}
