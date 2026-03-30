package com.libraryservice.factory;

import com.libraryservice.enums.SearchType;
import com.libraryservice.service.SearchableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchServiceFactoryTest {

    private SearchableService bookService;
    private SearchableService exerciseService;
    private SearchableService movieService;
    private SearchServiceFactory factory;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(SearchServiceFactoryTest.class.getName());
        logger.setLevel(Level.WARNING);
        //mock
        bookService = mock(SearchableService.class);
        when(bookService.getSearchType()).thenReturn(SearchType.BOOK);

        exerciseService = mock(SearchableService.class);
        when(exerciseService.getSearchType()).thenReturn(SearchType.EXERCISE);

        movieService = mock(SearchableService.class);
        when(movieService.getSearchType()).thenReturn(SearchType.MOVIE);

        factory = new SearchServiceFactory(List.of(bookService, exerciseService, movieService));
    }

    @Test
    void getService_returnsCorrectService() {
        // Book
        SearchableService result1 = factory.getService(SearchType.BOOK);
        assertEquals(bookService, result1);

        // Exercise
        SearchableService result2 = factory.getService(SearchType.EXERCISE);
        assertEquals(exerciseService, result2);

        // Movie
        SearchableService result3 = factory.getService(SearchType.MOVIE);
        assertEquals(movieService, result3);
    }

    @Test
    void getService_invalidType_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> factory.getService(null));
        assertTrue(exception.getMessage().contains("No service found"));
    }

    @Test
    void factory_callsUnderlyingServiceMethods() {
        //check mock behavior
        SearchableService service = factory.getService(SearchType.BOOK);
        service.search(null);
        service.findById("123");

        // Verify
        verify(bookService, times(1)).search(null);
        verify(bookService, times(1)).findById("123");
    }
}