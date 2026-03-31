package com.libraryservice.cache;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_cache")
public class BookCache {

    @Id
    private String isbn;

    @Column(columnDefinition = "TEXT") // Voor grote JSON strings
    private String jsonData;

    private LocalDateTime createdAt;

    // Default constructor voor JPA
    public BookCache() {
    }

    public BookCache(String isbn, String jsonData) {
        this.isbn = isbn;
        this.jsonData = jsonData;
        this.createdAt = LocalDateTime.now();
    }

    // De logica voor je experiment: is de data nog vers?
    public boolean isValid(int days) {
        return createdAt.isAfter(LocalDateTime.now().minusDays(days));
    }

    // Getters en Setters
    public String getIsbn() {
        return isbn;
    }

    public String getJsonData() {
        return jsonData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}