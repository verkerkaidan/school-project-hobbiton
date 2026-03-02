package com.postservice.postservice.models;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
public class Post {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private UUID id;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private UUID userId;

    @OneToMany
    @Column(nullable = false, name = "instance_list")
    private Set<MediaInstance> mediaInstanceSet;
}
