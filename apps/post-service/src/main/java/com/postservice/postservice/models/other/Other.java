package com.postservice.postservice.models.other;

import com.postservice.postservice.dto.OtherDTO;
import com.postservice.postservice.models.Post;
import jakarta.persistence.Entity;

/**
 * Other post — displayName, description, linkedItems, no hobby-specific fields.
 */
@Entity
public class Other extends Post {

    public Other() {}

    public Other(OtherDTO dto) {
        super(dto);
        super.setHobbyType("OTHER");
    }
}