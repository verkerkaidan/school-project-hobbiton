package com.postservice.postservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class MediaInstance {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private UUID id;  // JPA-generated primary key

    @Column(nullable = false)
    private UUID instanceId;  // the UUID passed in from the DTO

    @JoinColumn(nullable = false, name = "post")
    @ManyToOne
    @JsonIgnore
    private Post post;

    public MediaInstance(UUID instanceId, Post post) {
        this.instanceId = instanceId;  // ✅ store as a data field, not the PK
        this.post = post;
    }

    public MediaInstance() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}