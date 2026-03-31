//package com.libraryservice.services;
//
//import com.libraryservice.dto.book.BookResponse;
//import com.libraryservice.dto.common.SearchRequest;
//import com.libraryservice.dto.common.SearchResult;
//import com.libraryservice.model.Book;
//import com.libraryservice.service.BookService;
//import com.libraryservice.strategy.book.BookApiStrategy;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import static com.libraryservice.enums.SearchType.BOOK;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class BookServiceTest {
//
//    private BookApiStrategy strategy;
//    private BookService service;
//
//    @BeforeEach
//    void setUp() {
//        Logger logger = Logger.getLogger(BookService.class.getName());
//        logger.setLevel(Level.WARNING);
//        strategy = mock(BookApiStrategy.class);
//        service = new BookService(strategy);
//    }
//
//    @Test
//    void search_returnsMappedDTOs() {
//        Book book = new Book();
//        book.setTitle("Test Book");
//        book.setAuthors(List.of("Author 1"));
//        book.setIsbn(List.of("1234567890"));
//        book.setThumbnailUrl("https://covers.openlibrary.org/b/id/98765-M.jpg");
//
//        when(strategy.searchBooks("Test", 5)).thenReturn(List.of(book));
//
//        List<SearchResult> results = service.search(new SearchRequest(BOOK, "Test", 5));
//
//        assertEquals(1, results.size());
//        BookResponse response = (BookResponse) results.getFirst();
//        assertEquals("Test Book", response.getTitle());
//        assertEquals(List.of("Author 1"), response.getAuthors());
//        assertEquals("98765", response.getCoverId());
//    }
//
//    @Test
//    void findById_returnsMappedDTO() {
//        Book book = new Book();
//        book.setTitle("Book ISBN");
//        book.setIsbn(List.of("1234567890"));
//
//        when(strategy.findByIsbn("1234567890")).thenReturn(book);
//
//        SearchResult result = service.findById("1234567890");
//
//        assertNotNull(result);
//        BookResponse response = (BookResponse) result;
//        assertEquals("Book ISBN", response.getTitle());
//        assertEquals(List.of("1234567890"), response.getIsbn());
//    }
//
//    @Test
//    void findById_returnsNull_ifNotFound() {
//        when(strategy.findByIsbn("unknown")).thenReturn(null);
//        assertNull(service.findById("unknown"));
//    }
//}