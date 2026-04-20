package com.libraryservice.service;

import com.libraryservice.cache.BookCacheProvider;
import com.libraryservice.dto.book.BookResponse;
import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.dto.common.SearchResult;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Book;
import com.libraryservice.strategy.book.BookApiStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService implements SearchableService {
    private final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookApiStrategy bookApiStrategy;
    private final BookCacheProvider cacheProvider; // Spring injecteert de actieve strategie

    public BookService(BookApiStrategy bookApiStrategy, BookCacheProvider cacheProvider) {
        this.bookApiStrategy = bookApiStrategy;
        this.cacheProvider = cacheProvider;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.BOOK;
    }

    /**
     * Voor zoeken (search) is caching complexer.
     * Voor nu focussen we op findById voor het experiment,
     * maar je zou hier ook een cache-check op de 'query' kunnen doen.
     */
    @Override
    public List<SearchResult> search(SearchRequest request) {
        List<Book> books = bookApiStrategy.searchBooks(request.getQuery(), request.getMaxResults());
        return books.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SearchResult findById(String id) {
        long startTime = System.currentTimeMillis(); // Start de timer

        // 1. Probeer eerst de cache
        Optional<Book> cachedBook = cacheProvider.get(id);

        if (cachedBook.isPresent()) {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[EXPERIMENT] CACHE HIT voor ISBN: {} | Tijd: {}ms | Strategie: {}",
                    id, duration, cacheProvider.getClass().getSimpleName());
            return toResponse(cachedBook.get());
        }

        // 2. Cache MISS: Haal op bij de externe API
        logger.info("[EXPERIMENT] CACHE MISS voor ISBN: {}. API aanroepen...", id);
        Book book = bookApiStrategy.findByIsbn(id);

        if (book != null) {
            // 3. Sla op in de cache
            cacheProvider.put(id, book);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("[EXPERIMENT] API SUCCESS voor ISBN: {} | Totale tijd (incl. opslaan): {}ms",
                    id, duration);
            return toResponse(book);
        }

        return null;
    }

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