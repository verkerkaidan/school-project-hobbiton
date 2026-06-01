package com.apigateway.apigateway.models.reading;

import com.apigateway.apigateway.models.Interaction;
import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.models.MediaInstance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Reading extends Post {

    private String bookTitle;
    private String pagesRead;
    private String totalPagesRead;
    private boolean bookFinished;

    public Reading() {

    }

    public Reading(UUID id, String description, String title, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt, List<MediaInstance> mediaInstanceSet, String hobbyType, String bookTitle, String pagesRead, String totalPagesRead, boolean bookFinished, Interaction interaction) {
        super(id, description, title, userId, createdAt, updatedAt, mediaInstanceSet, hobbyType, interaction);
        this.bookTitle = bookTitle;
        this.pagesRead = pagesRead;
        this.totalPagesRead = totalPagesRead;
        this.bookFinished = bookFinished;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(String pagesRead) {
        this.pagesRead = pagesRead;
    }

    public String getTotalPagesRead() {
        return totalPagesRead;
    }

    public void setTotalPagesRead(String totalPagesRead) {
        this.totalPagesRead = totalPagesRead;
    }

    public boolean isBookFinished() {
        return bookFinished;
    }

    public void setBookFinished(boolean bookFinished) {
        this.bookFinished = bookFinished;
    }
}
