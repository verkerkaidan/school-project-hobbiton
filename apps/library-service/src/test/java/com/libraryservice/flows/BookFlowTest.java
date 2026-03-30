package com.libraryservice.flows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryservice.dao.OpenLibraryDAO;
import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.enums.SearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
class BookFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenLibraryDAO openLibraryDAO;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(BookFlowTest.class.getName());
        logger.setLevel(Level.WARNING);
    }

    @Test
    void search_books_fullFlow_success() throws Exception {
        Map<String, Object> rawBook = Map.of(
                "key", "/works/OL123",
                "title", "Clean Code",
                "author_name", List.of("Robert C. Martin"),
                "publisher", List.of("Prentice Hall"),
                "isbn", List.of("1234567890"),
                "language", List.of("en"),
                "first_publish_year", 2008,
                "number_of_pages_median", 464,
                "cover_i", 12345
        );

        when(openLibraryDAO.searchBooks("clean", 5))
                .thenReturn(List.of(rawBook));

        SearchRequest request = new SearchRequest(
                SearchType.BOOK,
                "clean",
                5
        );

        mockMvc.perform(post("/api/v1/library/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.data[0].resultType").value("book"))
                .andExpect(jsonPath("$.data[0].authors[0]").value("Robert C. Martin"));
    }

    @Test
    void search_books_noResults() throws Exception {
        when(openLibraryDAO.searchBooks("clean", 5))
                .thenReturn(List.of());

        SearchRequest request = new SearchRequest(
                SearchType.BOOK,
                "clean",
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
    void search_books_getEndpoint() throws Exception {
        Map<String, Object> rawBook = Map.of(
                "key", "/works/OL123",
                "title", "Clean Code"
        );

        when(openLibraryDAO.searchBooks("clean", 5))
                .thenReturn(List.of(rawBook));

        mockMvc.perform(get("/api/v1/library/search")
                        .param("query", "clean")
                        .param("type", "BOOK")
                        .param("maxResults", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Clean Code"));
    }

    @Test
    void findById_books_found() throws Exception {
        Map<String, Object> bookData = Map.of(
                "title", "Clean Code",
                "cover", Map.of("medium", "http://cover.jpg")
        );

        Map<String, Object> response = Map.of(
                "ISBN:123", bookData
        );

        when(openLibraryDAO.findByIsbn("123"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/library/BOOK/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Clean Code"));
    }

    @Test
    void findById_books_notFound() throws Exception {
        when(openLibraryDAO.findByIsbn("999"))
                .thenReturn(Map.of());

        mockMvc.perform(get("/api/v1/library/BOOK/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void search_books_invalidRequest() throws Exception {
        // query leeg → moet falen
        String invalidJson = """
                {
                    "type": "BOOK",
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