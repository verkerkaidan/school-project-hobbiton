package com.libraryservice.strategies;

import com.libraryservice.dao.MovieDAO;
import com.libraryservice.model.Movie;
import com.libraryservice.strategy.movie.MovieDbStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieDBStrategyTest {

    private MovieDAO movieDAO;
    private MovieDbStrategy strategy;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(MovieDbStrategy.class.getName());
        logger.setLevel(Level.WARNING);

        movieDAO = mock(MovieDAO.class);
        strategy = new MovieDbStrategy(movieDAO);
    }

    @Test
    void searchMovies_returnsMappedMovies() {
        Map<String, Object> rawMovie = getStringObjectMap();

        when(movieDAO.searchMovies("Test", 10)).thenReturn(List.of(rawMovie));

        List<Movie> result = strategy.searchMovies("Test", 10);

        assertEquals(1, result.size());
        Movie movie = result.getFirst();
        assertEquals("tt123", movie.getMovieId());
        assertEquals("Test Movie", movie.getTitle());
        assertEquals("Some plot", movie.getDescription());
        assertEquals("Action", movie.getGenres());
        assertEquals("movie", movie.getMediaType());
    }

    private static @NonNull Map<String, Object> getStringObjectMap() {
        Map<String, Object> rawMovie = new HashMap<>();
        rawMovie.put("imdbID", "tt123");
        rawMovie.put("Title", "Test Movie");
        rawMovie.put("Plot", "Some plot");
        rawMovie.put("Rated", "PG");
        rawMovie.put("Released", "2023");
        rawMovie.put("Runtime", "120 min");
        rawMovie.put("Year", "2023");
        rawMovie.put("Director", "Director Name");
        rawMovie.put("Writer", "Writer Name");
        rawMovie.put("Language", "English");
        rawMovie.put("Poster", "poster.jpg");
        rawMovie.put("Actors", "Actor1, Actor2");
        rawMovie.put("Genre", "Action");
        rawMovie.put("imdbRating", "7.5");
        rawMovie.put("Type", "movie");
        rawMovie.put("Response", "True");
        return rawMovie;
    }

    @Test
    void findById_returnsMovie_ifFound() {
        Map<String, Object> rawMovie = new HashMap<>();
        rawMovie.put("imdbID", "tt123");
        rawMovie.put("Title", "Test Movie");
        rawMovie.put("Plot", "Some plot");
        rawMovie.put("Genre", "Action");
        rawMovie.put("Type", "movie");
        rawMovie.put("Response", "True");

        when(movieDAO.findById("tt123")).thenReturn(rawMovie);

        Movie movie = strategy.findById("tt123");

        assertNotNull(movie);
        assertEquals("tt123", movie.getMovieId());
        assertEquals("Test Movie", movie.getTitle());
        assertEquals("Some plot", movie.getDescription());
        assertEquals("Action", movie.getGenres());
    }

    @Test
    void findById_returnsNull_ifNotFound() {
        when(movieDAO.findById("unknown")).thenReturn(null);

        Movie movie = strategy.findById("unknown");

        assertNull(movie);
    }
}