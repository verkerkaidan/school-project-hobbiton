package com.apigateway.apigateway.DTO.posts;

import com.apigateway.apigateway.models.Post;
import com.apigateway.apigateway.models.reading.Reading;

public class ReadingDTO extends PostDTO{

    public String bookTitle;
    public String pagesRead;
    public String totalPagesRead;
    public boolean bookFinished;
}
