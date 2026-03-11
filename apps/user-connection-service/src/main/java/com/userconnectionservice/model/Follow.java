package com.userconnectionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "follow", uniqueConstraints = @UniqueConstraint(columnNames = { "follower_id", "following_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "follower_id", nullable = true, columnDefinition = "uuid")
    private UUID follower;

    @Column(name = "following_id", nullable = true, columnDefinition = "uuid")
    private UUID following;

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    public Follow(UUID follower, UUID following) {
        this.follower = follower;
        this.following = following;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
