package com.postservice.postservice.models.other;

import com.postservice.postservice.dto.OtherDTO;
import com.postservice.postservice.models.Post;
import jakarta.persistence.Entity;

@Entity
public class Other extends Post {

    public Other(OtherDTO otherDTO) {
        super(otherDTO);  // was: super(postDTO)
        super.setHobbyType("OTHER");
    }

    public Other() {
    }
}
