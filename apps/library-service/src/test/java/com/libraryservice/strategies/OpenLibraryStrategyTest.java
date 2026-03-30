package com.libraryservice.strategies;

import com.libraryservice.dao.OpenLibraryDAO;
import com.libraryservice.model.Book;
import com.libraryservice.strategy.book.OpenLibraryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenLibraryStrategyTest {

    private OpenLibraryDAO dao;
    private OpenLibraryStrategy strategy;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(OpenLibraryStrategy.class.getName());
        logger.setLevel(Level.WARNING);
        dao = mock(OpenLibraryDAO.class);
        strategy = new OpenLibraryStrategy(dao);
    }

    @Test
    void searchBooks_returnsMappedBooks() {
        Map<String, Object> rawBook = new HashMap<>();
        rawBook.put("key", "OL1M");
        rawBook.put("title", "Test Book");
        rawBook.put("author_name", List.of("Author One"));
        rawBook.put("publisher", List.of("Publisher A"));
        rawBook.put("isbn", List.of("1234567890"));
        rawBook.put("language", List.of("English"));
        rawBook.put("first_publish_year", 2020);
        rawBook.put("number_of_pages_median", 200);
        rawBook.put("cover_i", 12345);

        when(dao.searchBooks("Test", 10)).thenReturn(List.of(rawBook));

        List<Book> books = strategy.searchBooks("Test", 10);

        assertEquals(1, books.size());
        Book book = books.getFirst();
        assertEquals("OL1M", book.getId());
        assertEquals("Test Book", book.getTitle());
        assertEquals(List.of("Author One"), book.getAuthors());
        assertEquals("https://covers.openlibrary.org/b/id/12345-M.jpg", book.getThumbnailUrl());
    }

    @Test
    void findByIsbn_returnsBook_ifFound() {
        Map<String, Object> isbnData = new HashMap<>();
        isbnData.put("title", "ISBN Book");
        isbnData.put("cover", Map.of("medium", "cover.jpg"));

        Map<String, Object> raw = new HashMap<>();
        raw.put("ISBN:123", isbnData);

        when(dao.findByIsbn("123")).thenReturn(raw);

        Book book = strategy.findByIsbn("123");

        assertNotNull(book);
        assertEquals(List.of("123"), book.getIsbn());
        assertEquals("ISBN Book", book.getTitle());
        assertEquals("cover.jpg", book.getThumbnailUrl());
    }

    @Test
    void findByIsbn_returnsNull_ifNotFound() {
        when(dao.findByIsbn("000")).thenReturn(new HashMap<>());

        Book book = strategy.findByIsbn("000");

        assertNull(book);
    }
}