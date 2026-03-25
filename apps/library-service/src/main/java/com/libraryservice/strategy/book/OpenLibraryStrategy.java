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
                .map(this::mapSearchDocToBook)
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
     * @param doc ruwe API data
     * @return Book model
     */
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
        return book;
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
