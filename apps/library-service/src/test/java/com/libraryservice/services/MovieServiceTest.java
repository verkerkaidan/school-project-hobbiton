package com.libraryservice.services;

import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.dto.movie.MovieResponse;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Movie;
import com.libraryservice.service.MovieService;
import com.libraryservice.strategy.movie.MovieApiStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    private MovieApiStrategy strategy;
    private MovieService service;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(MovieServiceTest.class.getName());
        logger.setLevel(Level.WARNING);
        strategy = mock(MovieApiStrategy.class);
        service = new MovieService(strategy);
    }

    @Test
    void search_returnsMappedDTOs() {
        Movie movie = new Movie("tt123", "Test Movie", "Plot", "PG", "2023", "120 min", "2023",
                "Director", "Writer", "English", "poster.jpg", "Actors", "Action", "7.5", "movie");

        when(strategy.searchMovies("Test", 5)).thenReturn(List.of(movie));

        List<SearchResult> results = service.search(new SearchRequest(SearchType.MOVIE, "Test", 5));

        assertEquals(1, results.size());
        MovieResponse response = (MovieResponse) results.getFirst();
        assertEquals("tt123", response.getMovieId());
        assertEquals("Test Movie", response.getTitle());
        assertEquals("Action", response.getGenres());
    }

    @Test
    void findById_returnsMappedDTO() {
        Movie movie = new Movie("tt123", "Test Movie", "Plot", "PG", "2023", "120 min", "2023",
                "Director", "Writer", "English", "poster.jpg", "Actors", "Action", "7.5", "movie");

        when(strategy.findById("tt123")).thenReturn(movie);

        SearchResult result = service.findById("tt123");

        assertNotNull(result);
        MovieResponse response = (MovieResponse) result;
        assertEquals("tt123", response.getMovieId());
        assertEquals("Test Movie", response.getTitle());
    }

    @Test
    void findById_returnsNull_ifNotFound() {
        when(strategy.findById("unknown")).thenReturn(null);
        assertNull(service.findById("unknown"));
    }
}