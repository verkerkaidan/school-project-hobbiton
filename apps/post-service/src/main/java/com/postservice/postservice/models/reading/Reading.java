package com.postservice.postservice.models.reading;

import com.postservice.postservice.dto.ReadingDTO;
import com.postservice.postservice.models.Post;
import jakarta.persistence.Entity;

@Entity
public class Reading extends Post {

    private String bookTitle;
    private String pagesRead;
    private String totalPagesRead;
    private boolean bookFinished;

    public Reading(ReadingDTO readingDTO){
        super(readingDTO);

        this.bookTitle = readingDTO.title;
        this.pagesRead = readingDTO.pagesRead;
        this.totalPagesRead = readingDTO.totalPagesRead;
        this.bookFinished = readingDTO.bookFinished;
    }

    public Reading() {

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
