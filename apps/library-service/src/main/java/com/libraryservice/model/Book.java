package com.libraryservice.model;

import java.util.List;

/**
 * Kern Model voor de Library Service.
 * Aangepast om consistent te zijn met de data types van de Open Library API.
 */
public class Book {

    private String id;
    private String title;
    private List<String> authors;
    private String description;
    private List<String> publisher;
    private String publishedDate;
    private List<String> isbn;
    private String pageCount;
    private List<String> language;
    private String thumbnailUrl;
    private String infoLink;
    private String averageRating;
    private String ratingsCount;

    // Default constructor
    public Book() {
    }

    // Volledige constructor
    public Book(String id, String title, List<String> authors, String description,
                List<String> publisher, String publishedDate, List<String> isbn,
                String pageCount, List<String> language, String thumbnailUrl,
                String infoLink, String averageRating, String ratingsCount) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.pageCount = pageCount;
        this.language = language;
        this.thumbnailUrl = thumbnailUrl;
        this.infoLink = infoLink;
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
    }

    // Getters en Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPublisher() {
        return publisher;
    }

    public void setPublisher(List<String> publisher) {
        this.publisher = publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public List<String> getIsbn() {
        return isbn;
    }

    public void setIsbn(List<String> isbn) {
        this.isbn = isbn;
    }

    public String getPageCount() {
        return pageCount;
    }

    public void setPageCount(String pageCount) {
        this.pageCount = pageCount;
    }

    public List<String> getLanguage() {
        return language;
    }

    public void setLanguage(List<String> language) {
        this.language = language;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
    }

    public String getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(String averageRating) {
        this.averageRating = averageRating;
    }

    public String getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(String ratingsCount) {
        this.ratingsCount = ratingsCount;
    }
}