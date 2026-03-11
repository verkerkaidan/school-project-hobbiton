package com.profileconnectionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ProfileConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "initiator_id", nullable = true, columnDefinition = "uuid")
    private UUID initiator;

    @Column(name = "target_id", nullable = true, columnDefinition = "uuid")
    private UUID target;

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    public ProfileConnection(UUID initiator, UUID target) {
        this.initiator = initiator;
        this.target = target;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
