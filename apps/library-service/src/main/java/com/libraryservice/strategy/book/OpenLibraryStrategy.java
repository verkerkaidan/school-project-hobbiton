package com.libraryservice.strategy.book;

import com.libraryservice.dao.OpenLibraryDAO;
import com.libraryservice.model.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * STRATEGY + ADAPTER — implementatie voor Open Library.
 * <p>
 * Verantwoordelijkheid:
 * - Ophalen van data via OpenLibraryDAO
 * - Omzetten van Open Library JSON-structuur naar het interne Book model
 * <p>
 * Adapter functie:
 * Open Library gebruikt een afwijkende JSON-structuur.
 * Deze class vertaalt die structuur naar het uniforme Book model.
 * <p>
 * Hierdoor blijft BookService volledig onafhankelijk van externe API-structuren.
 */
@Component
public class OpenLibraryStrategy implements BookApiStrategy {

    private static final Logger logger = Logger.getLogger(OpenLibraryStrategy.class.getName());
    private final OpenLibraryDAO openLibraryDAO;

    public OpenLibraryStrategy(OpenLibraryDAO openLibraryDAO) {
        this.openLibraryDAO = openLibraryDAO;
    }

    @Override
    public List<Book> searchBooks(String query, int maxResults) {
        return openLibraryDAO.searchBooks(query, maxResults).stream()
                .map(this::mapToBook)
                .collect(Collectors.toList());
    }

    @Override
    public Book findByIsbn(String isbn) {
        logger.info("OpenLibraryStrategy: ISBN lookup " + isbn);

        Map<String, Object> raw = openLibraryDAO.findByIsbn(isbn);
        if (raw == null || raw.isEmpty()) return null;

        String apiKey = "ISBN:" + isbn;
        if (raw.containsKey(apiKey)) {
            return mapIsbnDataToBook(isbn, (Map<String, Object>) raw.get(apiKey));
        }

        return null;
    }

    /**
     * Zet een Open Library search document om naar een Book object.
     *
     * @param raw ruwe API data
     * @return Book model
     */

    private Book mapToBook(Map<String, Object> raw) {
        Book book = new Book();
        book.setId((String) raw.get("key"));
        book.setTitle((String) raw.get("title"));
        book.setAuthors((List<String>) raw.get("author_name"));
        book.setPublisher((List<String>) raw.get("publisher"));
        book.setIsbn((List<String>) raw.get("isbn"));
        book.setLanguage((List<String>) raw.get("language"));

        Object year = raw.get("first_publish_year");
        book.setPublishedDate(year != null ? String.valueOf(year) : null);

        Object pages = raw.get("number_of_pages_median");
        book.setPageCount(pages != null ? String.valueOf(pages) : null);

        Object coverI = raw.get("cover_i");
        if (coverI != null) {
            book.setThumbnailUrl("https://covers.openlibrary.org/b/id/" + coverI + "-M.jpg");
        }
        return book;
    }

    /**
     * Zet ISBN-detail data om naar een Book object.
     *
     * @param isbn het ISBN-nummer
     * @param data ruwe API-data van Open Library
     * @return Book model
     */

    private Book mapIsbnDataToBook(String isbn, Map<String, Object> data) {
        Book book = new Book();
        book.setIsbn(List.of(isbn));
        book.setTitle((String) data.get("title"));

        if (data.containsKey("cover")) {
            Map<String, String> covers = (Map<String, String>) data.get("cover");
            book.setThumbnailUrl(covers.get("medium"));
        }
        return book;
    }
}