package com.libraryservice.repository;

import com.libraryservice.dao.BookDAO;
import com.libraryservice.enums.BookCategory;
import com.libraryservice.model.Book;
import com.libraryservice.provider.BookProvider;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Repository
public class GoogleBooksRepository implements BookProvider {

    private static final Logger logger = Logger.getLogger(GoogleBooksRepository.class.getName());

    private final BookDAO bookDAO;

    public GoogleBooksRepository(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    /**
     * Searches for books and returns a list of Book domain objects.
     * This is the only method the Service layer calls.
     */
    public List<Book> searchBooks(String query, BookCategory category, int maxResults) {
        String fullQuery = buildQuery(query, category);

        List<Map<String, Object>> rawItems = bookDAO.searchBooks(fullQuery, maxResults);

        return rawItems.stream()
                .map(item -> mapToBook(item, category))
                .collect(Collectors.toList());
    }

    /**
     * Builds the Google Books query string.
     * Google supports 'subject:' as a category filter.
     * Example: "clean code+subject:technology"
     */
    private String buildQuery(String query, BookCategory category) {
        if (category == null || category == BookCategory.ALL) {
            return query;
        }
        return query + "+subject:" + category.getLabel();
    }

    /**
     * ADAPTER PATTERN — converts a raw Google Books API item into a Book.
     * <p>
     * Google's structure is nested, inconsistent, and full of optional fields.
     * Our Book model is flat and typed. This method bridges the gap.
     * <p>
     * getOrDefault() is used everywhere because Google omits fields freely —
     * a book without a description, no publisher, zero-page count etc.
     * We never let a missing field crash the mapping.
     */
    @SuppressWarnings("unchecked")
    private Book mapToBook(Map<String, Object> item, BookCategory category) {
        Map<String, Object> volumeInfo = (Map<String, Object>) item.getOrDefault("volumeInfo", Collections.emptyMap());
        Map<String, Object> imageLinks = (Map<String, Object>) volumeInfo.getOrDefault("imageLinks", Collections.emptyMap());

        Book book = new Book();
        book.setBookId((String) item.get("id"));
        book.setTitle((String) volumeInfo.getOrDefault("title", "Unknown Title"));
        book.setAuthors((List<String>) volumeInfo.getOrDefault("authors", Collections.singletonList("Unknown Author")));
        book.setDescription((String) volumeInfo.getOrDefault("description", "No description available."));
        book.setPublisher((String) volumeInfo.getOrDefault("publisher", "Unknown Publisher"));
        book.setPublishedDate((String) volumeInfo.getOrDefault("publishedDate", "Unknown"));
        book.setIsbn(extractIsbn(volumeInfo));
        book.setPageCount((int) volumeInfo.getOrDefault("pageCount", 0));
        book.setThumbnailUrl((String) imageLinks.getOrDefault("thumbnail", ""));
        book.setCategory(category);
        book.setAverageRating(toDouble(volumeInfo.getOrDefault("averageRating", 0.0)));
        book.setRatingsCount((int) volumeInfo.getOrDefault("ratingsCount", 0));

        logger.info("Mapped book: " + book.getTitle());
        return book;
    }

    /**
     * Google returns ISBNs as a list of typed identifiers.
     * We prefer ISBN_13, fall back to ISBN_10, default to "N/A".
     */
    @SuppressWarnings("unchecked")
    private String extractIsbn(Map<String, Object> volumeInfo) {
        List<Map<String, String>> identifiers =
                (List<Map<String, String>>) volumeInfo.getOrDefault("industryIdentifiers", Collections.emptyList());

        return identifiers.stream()
                .filter(id -> "ISBN_13".equals(id.get("type")))
                .map(id -> id.get("identifier"))
                .findFirst()
                .orElseGet(() -> identifiers.stream()
                        .filter(id -> "ISBN_10".equals(id.get("type")))
                        .map(id -> id.get("identifier"))
                        .findFirst()
                        .orElse("N/A"));
    }

    /**
     * Google sometimes returns rating as Integer, sometimes as Double.
     * This helper handles both safely via the Number supertype.
     */
    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}

