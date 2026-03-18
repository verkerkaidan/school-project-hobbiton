package com.libraryservice.model;


import com.libraryservice.enums.BookCategory;

import java.util.List;

public class Book {
    private String bookId;
    private String title;
    private List<String> authors;
    private String publisher;
    private String isbn;
    private String publishedDate;
    private String description;
    private int pageCount;
    private String thumbnailUrl;
    private double averageRating;
    private BookCategory category;
    private int ratingsCount;

    //constructors
    public Book() {}

    public Book(String bookId, String title, List<String> authors, String publisher, String isbn, String publishedDate, String description, int pageCount, String thumbnailUrl, double averageRating, BookCategory category, int ratingsCount) {
        this.bookId = bookId;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.isbn = isbn;
        this.publishedDate = publishedDate;
        this.description = description;
        this.pageCount = pageCount;
        this.thumbnailUrl = thumbnailUrl;
        this.averageRating = averageRating;
        this.category = category;
        this.ratingsCount = ratingsCount;
    }

    //getters and setters

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public BookCategory getCategory() {
        return category;
    }

    public void setCategory(BookCategory category) {
        this.category = category;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    // For logging purposes:

    @Override
    public String toString(){
        return "Book{" +
                "bookId='" + bookId + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", publisher='" + publisher + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", description='" + description + '\'' +
                ", pageCount=" + pageCount +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", averageRating=" + averageRating +
                ", category=" + category +
                ", ratingsCount=" + ratingsCount +
                '}';
    }
}
