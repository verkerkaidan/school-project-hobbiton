package com.libraryservice.provider;

import com.libraryservice.dao.OpenLibraryDAO;
import com.libraryservice.enums.BookCategory;
import com.libraryservice.model.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class OpenLibraryProvider implements BookProvider {
    private final OpenLibraryDAO openLibraryDAO;

    public OpenLibraryProvider(OpenLibraryDAO openLibraryDAO) {
        this.openLibraryDAO = openLibraryDAO;
    }

    @Override
    public List<Book> searchBooks(String query, BookCategory category, int maxResults) {
        List<Map<String, Object>> items = openLibraryDAO.searchBooks(query, maxResults);
        return items.stream()
                .map(this::mapToBook)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Book mapToBook(Map<String, Object> item) {
        Book book = new Book();

        book.setTitle((String) item.getOrDefault("title", "Unknown"));
        book.setAuthors((List<String>) item.getOrDefault("author_name", List.of("Unknown")));
        book.setPublishedDate(String.valueOf(item.getOrDefault("first_publish_year", "Unknown")));
        book.setIsbn(extractIsbn(item));
        book.setPageCount(0);
        book.setDescription("No description available");
        book.setPublisher("Unknown");
        book.setThumbnailUrl("");
        book.setAverageRating(0.0);
        book.setRatingsCount(0);

        return book;
    }

    private String extractIsbn(Map<String, Object> item) {
        List<String> isbns = (List<String>) item.getOrDefault("isbn", List.of());
        return isbns.isEmpty() ? null : isbns.get(0);
    }

}
