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
    private String source;

    public BookResponse() {
    }

    @Override
    public String getResultType() {
        return "book";
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorDisplay() {
        return authorDisplay;
    }

    public String getDescription() {
        return description;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getLanguage() {
        return language;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public String getSource() {
        return source;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthorDisplay(String authorDisplay) {
        this.authorDisplay = authorDisplay;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
