package com.libraryservice.dto.book;

import com.libraryservice.dto.common.SearchResult;

/**
 * Book-specific response DTO.
 * Implements SearchResult so it can be returned from SearchableService
 * alongside ExerciseResponse without the controller caring which it is.
 */

public class BookResponse implements SearchResult {

    private String title;
    private String authorDisplay;
    private String description;
    private String publisher;
    private String publishedDate;
    private String isbn;
    private int pageCount;
    private String language;
    private String thumbnailUrl;
    private String infoLink;
    private double averageRating;
    private int ratingsCount;

    public BookResponse(String title, String authorDisplay, String truncate, String publisher, String publishedDate, String isbn, int pageCount, String language, String thumbnailUrl, String infoLink, double averageRating, int ratingsCount) {
    }

    public BookResponse() {
    }

    @Override
    public String getResultType() {
        return "book";
    }

}
