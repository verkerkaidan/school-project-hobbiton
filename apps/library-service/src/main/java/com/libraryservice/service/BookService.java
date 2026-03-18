package com.libraryservice.service;

import com.libraryservice.dto.book.BookResponse;
import com.libraryservice.dto.book.BookSearchRequest;
import com.libraryservice.model.Book;
import com.libraryservice.repository.MultiSourceBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger logger = Logger.getLogger(BookService.class.getName());

    private final MultiSourceBookRepository bookRepository;

    public BookService(MultiSourceBookRepository bookRepository) {

        this.bookRepository = bookRepository;
    }

    /**
     * Searches books based on the incoming request and returns BookResponse DTOs.
     * This is the single entry point the Controller calls.
     */
    public List<BookResponse> searchBooks(BookSearchRequest request) {
        logger.info("Searching books: query='" + request.getQuery()
                + "', category=" + request.getCategory()
                + ", maxResults=" + request.getMaxResults());

        List<Book> books = bookRepository.searchBooks(
                request.getQuery(),
                request.getCategory(),
                request.getMaxResults()
        );

        return books.stream()
                .map(this::toBookResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Book domain model to a BookResponse DTO.
     * <p>
     * 'authorDisplay' is computed here — joining a list like ["Tolkien", "Lee"]
     * into the string "Tolkien & Lee". This is a presentation decision:
     * the model stores the list, the DTO stores the display string.
     */
    private BookResponse toBookResponse(Book book) {
        String authorDisplay = (book.getAuthors() == null || book.getAuthors().isEmpty())
                ? "Unknown Author"
                : String.join(" & ", book.getAuthors());

        BookResponse response = new BookResponse();
        response.setTitle(book.getTitle());
        response.setAuthorDisplay(authorDisplay);
        response.setDescription(truncateDescription(book.getDescription()));
        response.setPublisher(book.getPublisher());
        response.setPublishedDate(book.getPublishedDate());
        response.setIsbn(book.getIsbn());
        response.setPageCount(book.getPageCount());
        response.setThumbnailUrl(book.getThumbnailUrl());
        response.setCategory(book.getCategory() != null ? book.getCategory().getLabel() : "unknown");
        response.setAverageRating(book.getAverageRating());
        response.setRatingsCount(book.getRatingsCount());
        return response;
    }

    /**
     * Business rule: Google descriptions can be very long.
     * We cap at 300 characters in responses to keep payloads lean.
     * The full description is always reachable via the infoLink.
     */
    private String truncateDescription(String description) {
        if (description == null) return "No description available.";
        return description.length() > 300
                ? description.substring(0, 297) + "..."
                : description;
    }
}
