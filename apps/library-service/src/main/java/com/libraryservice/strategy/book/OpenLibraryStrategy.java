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

    /**
     * Zoekt boeken via Open Library op basis van een query.
     *
     * @param query      zoekterm
     * @param maxResults maximaal aantal resultaten
     * @return lijst van Book objecten
     */
    @Override
    public List<Book> searchBooks(String query, int maxResults) {
        logger.info("OpenLibraryStrategy: searching '" + query + "'");
        return openLibraryDAO.searchBooks(query, maxResults).stream()
                .map(this::mapToBook)
                .collect(Collectors.toList());
    }

    /**
     * Haalt één boek op via ISBN.
     *
     * @param isbn ISBN nummer
     * @return Book object of null indien niet gevonden
     */
    @Override
    public Book findByIsbn(String isbn) {
        logger.info("OpenLibraryStrategy: ISBN lookup " + isbn);
        Map<String, Object> raw = openLibraryDAO.findByIsbn(isbn);
        if (raw == null || raw.isEmpty() || !raw.containsKey("ISBN:" + isbn)) return null;
        return mapIsbnDataToBook(isbn, (Map<String, Object>) raw.get("ISBN:" + isbn));
    }

    /**
     * Zet een Open Library search document om naar een Book object.
     *
     * @param raw ruwe API data
     * @return Book model
     */
    private Book mapToBook(Map<String, Object> raw) {
        return new Book(
                (String) raw.getOrDefault("id", "unknown"), // id
                (String) raw.getOrDefault("title", "Unknown Title"),
                (List<String>) raw.getOrDefault("authors", Collections.emptyList()),
                (String) raw.getOrDefault("description", "Unknown Description"),
                (String) raw.getOrDefault("publisher", "Unknown Publisher"),
                (String) raw.getOrDefault("publishedDate", "Unknown"),
                (String) raw.getOrDefault("ISBN", "Unknown"), // isbn op juiste plek
                toInt(raw.getOrDefault("pageCount", 0)),
                (String) raw.getOrDefault("language", "unknown"),
                (String) raw.getOrDefault("thumbnailUrl", ""),
                (String) raw.getOrDefault("infoLink", ""),
                ((Number) raw.getOrDefault("averageRating", 0.0)).doubleValue(),
                toInt(raw.getOrDefault("ratingsCount", 0))
        );
    }

    /**
     * Zet ISBN-detail data om naar een Book object.
     *
     * @param isbn het ISBN-nummer
     * @param data ruwe API-data van Open Library
     * @return Book model
     */
    private Book mapIsbnDataToBook(String isbn, Map<String, Object> data) {
        return new Book(
                (String) data.getOrDefault("key", "unknown"), // id
                (String) data.getOrDefault("title", "Unknown Title"),
                (List<String>) data.getOrDefault("authors", Collections.emptyList()),
                (String) data.getOrDefault("description", "Unknown Description"),
                (String) data.getOrDefault("publishers", "Unknown Publisher"),
                (String) data.getOrDefault("publish_date", "Unknown"),
                isbn,
                toInt(data.getOrDefault("number_of_pages", 0)),
                (String) data.getOrDefault("languages", "unknown"),
                (String) data.getOrDefault("cover", ""),
                (String) data.getOrDefault("url", ""),
                ((Number) data.getOrDefault("average_rating", 0.0)).doubleValue(),
                toInt(data.getOrDefault("ratings_count", 0))
        );
    }

    /**
     * Helper methode om een Object veilig om te zetten naar int.
     *
     * @param val input waarde
     * @return integer waarde of 0 bij ongeldige input
     */
    private int toInt(Object val) {
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }
}
