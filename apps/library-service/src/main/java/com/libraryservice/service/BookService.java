package com.libraryservice.service;

import com.libraryservice.dto.book.BookResponse;
import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Book;
import com.libraryservice.strategy.book.BookApiStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private final BookApiStrategy bookApiStrategy;

    public BookService(BookApiStrategy bookApiStrategy) {
        this.bookApiStrategy = bookApiStrategy;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.BOOK;
    }

    @Override
    public List<SearchResult> search(SearchRequest request) {
        List<Book> books = bookApiStrategy.searchBooks(request.getQuery(), request.getMaxResults());
        return books.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SearchResult findById(String id) {
        Book book = bookApiStrategy.findByIsbn(id);
        return (book != null) ? toResponse(book) : null;
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
        BookResponse response = new BookResponse();

        response.setTitle(book.getTitle());
        response.setAuthors(book.getAuthors());
        response.setDescription(book.getDescription());
        response.setPublisher(book.getPublisher());
        response.setPublishedDate(book.getPublishedDate());
        response.setIsbn(book.getIsbn());
        response.setPageCount(book.getPageCount());
        response.setLanguage(book.getLanguage());

        if (book.getThumbnailUrl() != null && book.getThumbnailUrl().contains("/id/")) {
            String url = book.getThumbnailUrl();
            String id = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("-"));
            response.setCoverId(id);
        }

        return response;
    }
}