package com.libraryservice.controller;

import com.libraryservice.dao.BookDAO;
import com.libraryservice.dao.OpenLibraryDAO;
import com.libraryservice.dto.book.BookResponse;
import com.libraryservice.dto.book.BookSearchRequest;
import com.libraryservice.dto.common.ApiResponse;
import com.libraryservice.enums.BookCategory;
import com.libraryservice.service.BookService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookDAO.class);
    private final BookService bookService;
    private final OpenLibraryDAO openLibraryDAO;


    public BookController(BookService bookService, OpenLibraryDAO openLibraryDAO) {
        this.bookService = bookService;
        this.openLibraryDAO = openLibraryDAO;
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchBooksPost(
            @Valid @RequestBody BookSearchRequest request) {
        logger.info("Received book search request: query='{}', category={}, maxResults={}",
                request.getQuery(), request.getCategory(), request.getMaxResults());

        List<BookResponse> books = bookService.searchBooks(request);
        return ResponseEntity.ok(ApiResponse.success(books, books.size(), "Books found"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchBooksGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "ALL") BookCategory category,
            @RequestParam(defaultValue = "10") int maxResults) {

        logger.info("GET /search — query: " + query);

        BookSearchRequest request = new BookSearchRequest(query, category, maxResults);
        List<BookResponse> books = bookService.searchBooks(request);

        return ResponseEntity.ok(
                ApiResponse.success(books, books.size(), "Books retrieved successfully")
        );
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Book Microservice is running", 1, "OK")
        );
    }

    @GetMapping("/test-openlibrary")
    public ResponseEntity<?> testOpenLibrary() {
        List<Map<String, Object>> result = openLibraryDAO.searchBooks("fourth wing", 5);
        return ResponseEntity.ok(result);
    }
}
