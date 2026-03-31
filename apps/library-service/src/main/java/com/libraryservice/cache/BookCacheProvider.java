package com.libraryservice.cache;

import com.libraryservice.model.Book;

import java.util.Optional;

public interface BookCacheProvider {
    Optional<Book> get(String isbn);

    void put(String isbn, Book book);
}