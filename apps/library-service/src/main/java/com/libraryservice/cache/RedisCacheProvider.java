package com.libraryservice.cache;

import com.libraryservice.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "cache.strategy", havingValue = "redis")
public class RedisCacheProvider implements BookCacheProvider {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheProvider.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "book_cache:";

    public RedisCacheProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<Book> get(String isbn) {
        Book book = (Book) redisTemplate.opsForValue().get(KEY_PREFIX + isbn);
        if (book != null) {
            logger.info("Cache HIT (Redis) for ISBN: {}", isbn);
            return Optional.of(book);
        }
        return Optional.empty();
    }

    @Override
    public void put(String isbn, Book book) {
        // We zetten de TTL op 30 dagen. Redis flikkert het er daarna zelf uit.
        redisTemplate.opsForValue().set(KEY_PREFIX + isbn, book, 30, TimeUnit.DAYS);
        logger.info("Saved book {} to Redis cache (TTL: 30 days)", isbn);
    }
}