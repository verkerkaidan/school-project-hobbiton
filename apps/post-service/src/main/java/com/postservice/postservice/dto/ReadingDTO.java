package com.postservice.postservice.dto;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.reading.Reading;

public class ReadingDTO extends PostDTO{

    public String bookTitle;
    public String pagesRead;
    public String totalPagesRead;
    public boolean bookFinished;
    @Override
    public Post toEntity() {
        return new Reading(this);
    }
}
