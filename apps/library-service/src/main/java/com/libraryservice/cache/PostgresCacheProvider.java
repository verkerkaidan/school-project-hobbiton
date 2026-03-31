package com.libraryservice.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryservice.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnProperty(name = "cache.strategy", havingValue = "postgres")
public class PostgresCacheProvider implements BookCacheProvider {

    private static final Logger logger = LoggerFactory.getLogger(PostgresCacheProvider.class);
    private final CachedBookRepository repository;
    private final ObjectMapper objectMapper;

    public PostgresCacheProvider(CachedBookRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Book> get(String isbn) {
        return repository.findById(isbn).flatMap(cached -> {
            if (cached.isValid(30)) {
                try {
                    logger.info("Cache HIT (Postgres) for ISBN: {}", isbn);
                    return Optional.of(objectMapper.readValue(cached.getJsonData(), Book.class));
                } catch (Exception e) {
                    return Optional.empty();
                }
            } else {
                logger.info("Cache EXPIRED (Postgres) for ISBN: {}. Deleting...", isbn);
                repository.delete(cached);
                return Optional.empty();
            }
        });
    }

    @Override
    public void put(String isbn, Book book) {
        try {
            String json = objectMapper.writeValueAsString(book);
            BookCache cachedBook = new BookCache(isbn, json);
            repository.save(cachedBook);
            logger.info("Saved book {} to Postgres cache", isbn);
        } catch (Exception e) {
            logger.error("Failed to serialize book for Postgres cache", e);
        }
    }
}