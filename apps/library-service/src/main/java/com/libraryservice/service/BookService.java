package com.libraryservice.service;

import com.libraryservice.dto.book.BookResponse;
import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Book;
import com.libraryservice.strategy.book.BookApiStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * FACTORY PATTERN — this is what the factory produces for SearchType.BOOK.
 * STRATEGY PATTERN — internally selects the right BookApiStrategy.
 * <p>
 * This service implements SearchableService, which means:
 * - The Factory can return it as a SearchableService
 * - The LibraryController never sees "BookService" — only SearchableService
 * <p>
 * Internally it holds a List<BookApiStrategy> (Google, OpenLibrary) and
 * picks the right one based on request.getSource().
 * <p>
 * Spring auto-collects all @Component classes that implement BookApiStrategy
 * into the injected List — same collection injection pattern as before.
 */
@Service
public class BookService implements SearchableService {

    private static final Logger logger = Logger.getLogger(BookService.class.getName());

    private final List<BookApiStrategy> strategies;

    public BookService(List<BookApiStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.BOOK;
    }

    @Override
    public List<SearchResult> search(SearchRequest request) {
        logger.info("BookService.search: query='" + request.getQuery() + "', source=" + request.getSource());

        List<Book> books = new ArrayList<>();

        if ("all".equalsIgnoreCase(request.getSource())) {
            for (BookApiStrategy strategy : strategies) {
                books.addAll(strategy.searchBooks(request.getQuery(), request.getMaxResults()));
            }
        } else {
            BookApiStrategy strategy = findStrategy(request.getSource());
            books = strategy.searchBooks(request.getQuery(), request.getMaxResults());
        }

        return books.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SearchResult findById(String isbn) {
        logger.info("BookService.findById: ISBN=" + isbn);

        for (BookApiStrategy strategy : strategies) {
            Book book = strategy.findByIsbn(isbn);
            if (book != null) return toResponse(book);
        }
        return null;
    }

    private BookApiStrategy findStrategy(String name) {
        return strategies.stream()
                .filter(s -> s.getStrategyName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown source: '" + name + "'. Valid values: openlibrary, all"));
    }

    private BookResponse toResponse(Book book) {
        String authorDisplay = (book.getAuthors() == null || book.getAuthors().isEmpty())
                ? "Unknown Author"
                : String.join(" & ", book.getAuthors());

        BookResponse r = new BookResponse();
        r.setTitle(book.getTitle());
        r.setAuthorDisplay(authorDisplay);
        r.setDescription(truncate(book.getDescription()));
        r.setPublisher(book.getPublisher());
        r.setPublishedDate(book.getPublishedDate());
        r.setIsbn(book.getIsbn());
        r.setPageCount(book.getPageCount());
        r.setLanguage(book.getLanguage());
        r.setThumbnailUrl(book.getThumbnailUrl());
        r.setInfoLink(book.getInfoLink());
        r.setAverageRating(book.getAverageRating());
        r.setRatingsCount(book.getRatingsCount());
        r.setSource(book.getSource());
        return r;
    }

    private String truncate(String text) {
        if (text == null) return "No description available.";
        return text.length() > 300 ? text.substring(0, 297) + "..." : text;
    }
}
