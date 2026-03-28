package com.libraryservice.dto.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.libraryservice.dto.common.SearchResult;

import java.util.List;

/**
 * Book-specific response DTO.
 * Geoptimaliseerd voor de Open Library Search API.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true) // Voorkomt crashes bij onbekende velden
public class BookResponse implements SearchResult {

    private String title;

    @JsonProperty("author_name")
    private List<String> authors;

    private String description;

    @JsonProperty("publisher")
    private List<String> publisher;

    @JsonProperty("first_publish_year")
    private String publishedDate;

    @JsonProperty("isbn")
    private List<String> isbn;

    @JsonProperty("number_of_pages_median")
    private String pageCount;

    @JsonProperty("language")
    private List<String> language;

    @JsonProperty("cover_i")
    private String coverId;

    public BookResponse() {
    }

    public BookResponse(String title, List<String> authors, String description, List<String> publisher, String publishedDate, List<String> isbn, String pageCount, List<String> language, String coverId) {
        this.title = title;
        this.authors = authors;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.pageCount = pageCount;
        this.language = language;
        this.coverId = coverId;
    }

    @Override
    public String getResultType() {
        return "book";
    }

    // CUSTOM LOGICA VOOR DE COVER URL
    @JsonProperty("thumbnailUrl")
    public String getThumbnailUrl() {
        if (coverId != null && !coverId.isEmpty()) {
            return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
        }
        return null;
    }

    // GETTERS & SETTERS
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

    public String getCoverId() {
        return coverId;
    }

    public void setCoverId(String coverId) {
        this.coverId = coverId;
    }
}