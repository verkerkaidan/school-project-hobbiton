package com.libraryservice.flows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Movie;
import com.libraryservice.strategy.movie.MovieApiStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovieFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieApiStrategy movieApiStrategy;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(MovieFlowTest.class.getName());
        logger.setLevel(Level.WARNING);
    }

    @Test
    void search_movies_fullFlow_success() throws Exception {
        Map<String, Object> rawMovie = new HashMap<>();
        rawMovie.put("imdbID", "tt0132351");
        rawMovie.put("Title", "Clean Code: The Movie");
        rawMovie.put("Plot", "A movie about clean coding principles.");
        rawMovie.put("Rated", "PG");
        rawMovie.put("Released", "2008-08-01");
        rawMovie.put("Runtime", "120 min");
        rawMovie.put("Year", "2008");
        rawMovie.put("Director", "Robert Martin");
        rawMovie.put("Writer", "Robert Martin");
        rawMovie.put("Language", "en");
        rawMovie.put("Poster", "http://poster.jpg");
        rawMovie.put("Actors", "Robert C. Martin");
        rawMovie.put("Genre", "Education");
        rawMovie.put("imdbRating", "9.5");
        rawMovie.put("Type", "movie");
        rawMovie.put("Response", "True");

        Movie movie = new Movie(
                (String) rawMovie.get("imdbID"),
                (String) rawMovie.get("Title"),
                (String) rawMovie.get("Plot"),
                (String) rawMovie.get("Rated"),
                (String) rawMovie.get("Released"),
                (String) rawMovie.get("Runtime"),
                (String) rawMovie.get("Year"),
                (String) rawMovie.get("Director"),
                (String) rawMovie.get("Writer"),
                (String) rawMovie.get("Language"),
                (String) rawMovie.get("Poster"),
                (String) rawMovie.get("Actors"),
                (String) rawMovie.get("Genre"),
                (String) rawMovie.get("imdbRating"),
                (String) rawMovie.get("Type")
        );

        when(movieApiStrategy.searchMovies("clean", 5))
                .thenReturn(List.of(movie));

        SearchRequest request = new SearchRequest(
                SearchType.MOVIE,
                "clean",
                5
        );

        mockMvc.perform(post("/api/v1/library/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Clean Code: The Movie"))
                .andExpect(jsonPath("$.data[0].resultType").value("movie"))
                .andExpect(jsonPath("$.data[0].director").value("Robert Martin"));
    }

    @Test
    void search_movies_noResults() throws Exception {
        when(movieApiStrategy.searchMovies("unknown", 5))
                .thenReturn(List.of());

        SearchRequest request = new SearchRequest(
                SearchType.MOVIE,
                "unknown",
                5
        );

        mockMvc.perform(post("/api/v1/library/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void findById_movies_found() throws Exception {
        Movie movie = new Movie(
                "tt0132351",
                "Clean Code: The Movie",
                "A movie about clean coding principles.",
                "PG",
                "2008-08-01",
                "120 min",
                "2008",
                "Robert Martin",
                "Robert Martin",
                "en",
                "http://poster.jpg",
                "Robert C. Martin",
                "Education",
                "9.5",
                "movie"
        );

        when(movieApiStrategy.findById("tt0132351"))
                .thenReturn(movie);

        mockMvc.perform(get("/api/v1/library/MOVIE/tt0132351"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Clean Code: The Movie"))
                .andExpect(jsonPath("$.data.director").value("Robert Martin"));
    }

    @Test
    void findById_movies_notFound() throws Exception {
        when(movieApiStrategy.findById("unknown"))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/library/MOVIE/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void search_movies_invalidRequest() throws Exception {
        String invalidJson = """
                {
                    "type": "MOVIE",
                    "query": "",
                    "maxResults": 5
                }
                """;

        mockMvc.perform(post("/api/v1/library/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void health_endpoint_works() throws Exception {
        mockMvc.perform(get("/api/v1/library/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Library Microservice is running"));
    }
}