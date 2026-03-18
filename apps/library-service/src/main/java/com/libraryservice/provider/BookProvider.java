package com.libraryservice.provider;

import com.libraryservice.enums.BookCategory;
import com.libraryservice.model.Book;

import java.util.List;

public interface BookProvider {
    List<Book> searchBooks(String query, BookCategory category, int maxResults);
}
