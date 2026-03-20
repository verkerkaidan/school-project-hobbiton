package com.libraryservice.strategy.book;

import com.libraryservice.model.Book;

import java.util.List;

/**
 * STRATEGY PATTERN — interface for book API sources.
 * <p>
 * Every book API (Google Books, Open Library, future ones) implements this.
 * BookService only knows about BookApiStrategy — never the concrete classes.
 * <p>
 * getStrategyName() is used by BookService to select the right strategy
 * based on the 'source' field in the request ("google", "openlibrary", "all").
 */
public interface BookApiStrategy {

    String getStrategyName();

    List<Book> searchBooks(String query, int maxResults);

    Book findByIsbn(String isbn);
}
