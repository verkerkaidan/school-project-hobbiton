package com.libraryservice.strategy.book;

import com.libraryservice.dao.OpenLibraryDAO;
import com.libraryservice.model.Book;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * STRATEGY + ADAPTER — Open Library implementation.
 * <p>
 * Open Library's JSON is structured completely differently from Google Books.
 * Search docs: { "title", "author_name": [], "isbn": [], "first_publish_year" }
 * ISBN data:   { "title", "authors": [{"name":"..."}], "publishers": [{"name":"..."}] }
 * <p>
 * All of that complexity is hidden here. BookService sees only BookApiStrategy.
 */
@Component
public class OpenLibraryStrategy implements BookApiStrategy {

    private static final Logger logger = Logger.getLogger(OpenLibraryStrategy.class.getName());
    private static final String STRATEGY_NAME = "openlibrary";

    private final OpenLibraryDAO openLibraryDAO;

    public OpenLibraryStrategy(OpenLibraryDAO openLibraryDAO) {
        this.openLibraryDAO = openLibraryDAO;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public List<Book> searchBooks(String query, int maxResults) {
        logger.info("OpenLibraryStrategy: searching '" + query + "'");
        return openLibraryDAO.searchBooks(query, maxResults).stream()
                .map(this::mapSearchDocToBook)
                .collect(Collectors.toList());
    }

    @Override
    public Book findByIsbn(String isbn) {
        logger.info("OpenLibraryStrategy: ISBN lookup " + isbn);
        Map<String, Object> raw = openLibraryDAO.findByIsbn(isbn);
        if (raw == null || raw.isEmpty() || !raw.containsKey("ISBN:" + isbn)) return null;
        return mapIsbnDataToBook(isbn, (Map<String, Object>) raw.get("ISBN:" + isbn));
    }

    private Book mapSearchDocToBook(Map<String, Object> doc) {
        Book book = new Book();
        book.setTitle((String) doc.getOrDefault("title", "Unknown Title"));
        book.setAuthors((List<String>) doc.getOrDefault("author_name", Collections.singletonList("Unknown Author")));
        book.setPublishedDate(String.valueOf(doc.getOrDefault("first_publish_year", "Unknown")));
        book.setPageCount(toInt(doc.getOrDefault("number_of_pages_median", 0)));

        List<String> isbns = (List<String>) doc.getOrDefault("isbn", Collections.emptyList());
        book.setIsbn(isbns.isEmpty() ? "N/A" : isbns.getFirst());

        List<String> publishers = (List<String>) doc.getOrDefault("publisher", Collections.emptyList());
        book.setPublisher(publishers.isEmpty() ? "Unknown Publisher" : publishers.getFirst());

        List<String> languages = (List<String>) doc.getOrDefault("language", Collections.emptyList());
        book.setLanguage(languages.isEmpty() ? "unknown" : languages.getFirst());

        Object coverId = doc.get("cover_i");
        book.setThumbnailUrl(coverId != null ? "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg" : "");

        String olKey = (String) doc.getOrDefault("key", "");
        book.setInfoLink("https://openlibrary.org" + olKey);
        book.setId(olKey);
        book.setDescription("No description available.");
        book.setAverageRating(0.0);
        book.setRatingsCount(0);
        book.setSource(STRATEGY_NAME);
        return book;
    }

    private Book mapIsbnDataToBook(String isbn, Map<String, Object> data) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle((String) data.getOrDefault("title", "Unknown Title"));

        List<Map<String, String>> authorList = (List<Map<String, String>>) data.getOrDefault("authors", Collections.emptyList());
        List<String> authorNames = authorList.stream().map(a -> a.getOrDefault("name", "Unknown")).collect(Collectors.toList());
        book.setAuthors(authorNames.isEmpty() ? Collections.singletonList("Unknown Author") : authorNames);

        List<Map<String, String>> publisherList = (List<Map<String, String>>) data.getOrDefault("publishers", Collections.emptyList());
        book.setPublisher(publisherList.isEmpty() ? "Unknown Publisher" : publisherList.getFirst().getOrDefault("name", "Unknown Publisher"));

        book.setPublishedDate((String) data.getOrDefault("publish_date", "Unknown"));
        book.setPageCount(toInt(data.getOrDefault("number_of_pages", 0)));

        Map<String, String> cover = (Map<String, String>) data.getOrDefault("cover", Collections.emptyMap());
        book.setThumbnailUrl(cover.getOrDefault("medium", ""));
        book.setInfoLink((String) data.getOrDefault("url", ""));
        book.setId(isbn);
        book.setLanguage("unknown");
        book.setDescription("No description available.");
        book.setAverageRating(0.0);
        book.setRatingsCount(0);
        book.setSource(STRATEGY_NAME);
        return book;
    }

    private int toInt(Object val) {
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }
}
