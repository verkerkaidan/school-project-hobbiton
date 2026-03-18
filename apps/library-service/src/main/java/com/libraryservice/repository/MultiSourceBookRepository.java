package com.libraryservice.repository;

import com.libraryservice.enums.BookCategory;
import com.libraryservice.model.Book;
import com.libraryservice.provider.BookProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MultiSourceBookRepository {

    private final List<BookProvider> bookProviders;

    public MultiSourceBookRepository(List<BookProvider> bookProviders) {
        this.bookProviders = bookProviders;
    }

    public List<Book> searchBooks(String query, BookCategory category, int maxResults) {
        return bookProviders.stream()
                .flatMap(provider -> provider.searchBooks(query, category, maxResults).stream())
                .limit(maxResults)
                .toList();
    }
}
