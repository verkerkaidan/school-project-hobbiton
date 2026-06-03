use serde_json::Value;
use crate::dto::response::{MovieResponse, SearchResult};
use crate::models::movie::Movie;
use tracing::debug;

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

