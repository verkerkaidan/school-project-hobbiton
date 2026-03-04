package com.postservice.postservice.dto;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class PostDTO {
    public UUID id;
    public String description;
    public String title;
    public UUID userId;
    public Set<UUID> mediaInstanceSet;
}
