package com.libraryservice.dto.book;

public class BookResponse {
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
    private String category;
    private double averageRating;
    private int ratingsCount;

    // -------------------------
    // Constructors
    // -------------------------

    public BookResponse() {
    }

    public BookResponse(String title, String authorDisplay, String description,
                        String publisher, String publishedDate, String isbn,
                        int pageCount, String language, String thumbnailUrl,
                        String infoLink, String category,
                        double averageRating, int ratingsCount) {
        this.title = title;
        this.authorDisplay = authorDisplay;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.pageCount = pageCount;
        this.language = language;
        this.thumbnailUrl = thumbnailUrl;
        this.infoLink = infoLink;
        this.category = category;
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
    }

    // -------------------------
    // Getters
    // -------------------------

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

    public String getCategory() {
        return category;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    // -------------------------
    // Setters
    // -------------------------

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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }
}
