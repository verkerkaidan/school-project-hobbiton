package com.postservice.postservice.models;

import com.postservice.postservice.dto.PostDTO;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditingEntityListener.class)
public abstract class Post {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private UUID userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "post", fetch = FetchType.EAGER)
    private Set<MediaInstance> mediaInstanceSet;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private Set<LinkedHobbyItem> linkedItems = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String hobbyType;

    public Post() {
    }

    public Post(PostDTO dto) {
        this.id = dto.id;
        this.description = dto.description;
        this.title = dto.title;
        this.userId = dto.userId;

        if (dto.linkedItems != null) {
            this.linkedItems = dto.linkedItems.stream().map(i -> {
                LinkedHobbyItem item = new LinkedHobbyItem();
                item.setDisplayName(i.displayName);
                item.setImageUrl(i.imageUrl);
                item.setExternalId(i.externalId);
                item.setHobbyType(i.hobbyType);
                item.setPost(this);
                return item;
            }).collect(Collectors.toSet());
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Set<MediaInstance> getMediaInstanceSet() {
        return mediaInstanceSet;
    }

    public void setMediaInstanceSet(Set<MediaInstance> mediaInstanceSet) {
        this.mediaInstanceSet = mediaInstanceSet;
    }

    public void addMediaInstanceToSet(MediaInstance mediaInstance) {
        this.mediaInstanceSet.add(mediaInstance);
    }

    public void removeMediaInstanceFromSet(MediaInstance mediaInstance) {
        this.mediaInstanceSet.remove(mediaInstance);
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHobbyType() {
        return hobbyType;
    }

    public void setHobbyType(String hobbyType) {
        this.hobbyType = hobbyType;
    }

    public Set<LinkedHobbyItem> getLinkedItems() {
        return linkedItems;
    }

    public void setLinkedItems(Set<LinkedHobbyItem> linkedItems) {
        this.linkedItems = linkedItems;
    }

    public void addLinkedItem(LinkedHobbyItem item) {
        item.setPost(this);
        this.linkedItems.add(item);
    }
}
