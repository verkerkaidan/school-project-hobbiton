package com.postservice.postservice.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class MediaInstance {
    @Id
    @GeneratedValue
    @Column(nullable = false, name = "instance_id")
    private UUID instanceId;

    @Id
    @JoinColumn(nullable = false, name = "post")
    @ManyToOne
    private Post post;

}
