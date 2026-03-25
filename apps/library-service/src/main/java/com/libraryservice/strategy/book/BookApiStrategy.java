package com.libraryservice.strategy.book;

import com.libraryservice.model.Book;

import java.util.List;

/**
 * STRATEGY PATTERN — interface voor boek API-bronnen.
 * Elke boek API (bijv. Google Books, Open Library of toekomstige API’s)
 * implementeert deze interface.
 * <p>
 * BookService werkt alleen met BookApiStrategy en kent de concrete
 * implementaties niet.
 * <p>
 * De methode getStrategyName() wordt gebruikt door BookService om de juiste
 */
public interface BookApiStrategy {
    List<Book> searchBooks(String query, int maxResults);

    Book findByIsbn(String isbn);
}
