package com.userconnectionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "block", uniqueConstraints = @UniqueConstraint(columnNames = { "blocker_id", "blocked_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "blocker_id", nullable = true, columnDefinition = "uuid")
    private UUID blocker;

    @Column(name = "blocked_id", nullable = true, columnDefinition = "uuid")
    private UUID blocked;

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
