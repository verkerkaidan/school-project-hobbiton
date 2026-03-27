package com.libraryservice.strategy.movie;

import com.libraryservice.model.Movie;

import java.util.List;

public interface MovieApiStrategy {
    List<Movie> searchMovies(String query, int maxResults);

    Movie findById(String movieId);
}
