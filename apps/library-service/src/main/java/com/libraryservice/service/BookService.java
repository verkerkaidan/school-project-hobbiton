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
 * Service voor het verwerken van boek-zoekopdrachten.
 * <p>
 * Design patterns:
 * - Factory Pattern: deze service wordt geleverd door de SearchServiceFactory
 * - Strategy Pattern: gebruikt meerdere BookApiStrategy implementaties (bijv. OpenLibrary, Google)
 * <p>
 * Verantwoordelijkheden:
 * - Zoeken van boeken via één of meerdere API-strategieën
 * - Ophalen van boeken op basis van ISBN
 * - Omzetten van domain object (Book) naar DTO (BookResponse)
 * <p>
 * Spring injecteert automatisch alle BookApiStrategy implementaties
 * in de List<BookApiStrategy>.
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

    /**
     * Voert een zoekopdracht uit voor boeken.
     * <p>
     * Flow:
     * 1. Itereert over alle beschikbare BookApiStrategy implementaties
     * 2. Roept elke API aan en verzamelt resultaten
     * 3. Zet alle Book objecten om naar BookResponse DTO’s
     * <p>
     * Voordeel:
     * - Resultaten kunnen uit meerdere bronnen tegelijk komen
     *
     * @param request bevat zoekterm en maxResults
     * @return lijst van SearchResult (BookResponse)
     */
    @Override
    public List<SearchResult> search(SearchRequest request) {
        logger.info("BookService.search: query='" + request.getQuery() + "'");

        List<Book> books = new ArrayList<>();

        for (BookApiStrategy strategy : strategies) {
            books.addAll(strategy.searchBooks(request.getQuery(), request.getMaxResults()));
        }

        return books.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Haalt één boek op via ISBN.
     * <p>
     * Doorloopt alle beschikbare strategieën totdat er een resultaat gevonden wordt.
     *
     * @param isbn het ISBN van het boek
     * @return SearchResult (BookResponse) of null indien niet gevonden
     */
    @Override
    public SearchResult findById(String isbn) {
        logger.info("BookService.findById: ISBN=" + isbn);

        for (BookApiStrategy strategy : strategies) {
            Book book = strategy.findByIsbn(isbn);
            if (book != null) return toResponse(book);
        }
        return null;
    }

    /**
     * Zet een Book domain object om naar een BookResponse DTO.
     * <p>
     * Bevat extra logica zoals:
     * - Formattering van auteurs
     * - Afkorten van lange beschrijvingen
     *
     * @param book het domain model
     * @return DTO geschikt voor API-response
     */
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

    /**
     * Kort een tekst af tot maximaal 300 karakters.
     *
     * @param text input tekst
     * @return afgekorte tekst of fallback tekst indien null
     */
    private String truncate(String text) {
        if (text == null) return "No description available.";
        return text.length() > 300 ? text.substring(0, 297) + "..." : text;
    }
}
