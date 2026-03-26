package com.profileconnectionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log_profile_connections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The action performed (e.g., "DELETE_USER_BLOCKS", "DELETE_USER_FOLLOWS",
     * "DELETE_USER_REPORTS")
     */
    @Column(nullable = false)
    private String action;

    /**
     * The user UUID associated with the deletion
     */
    @Column(nullable = false)
    private UUID userUUID;

    /**
     * Record type deleted (e.g., "BLOCK", "FOLLOW", "REPORT")
     */
    @Column(nullable = false)
    private String recordType;

    /**
     * Number of records deleted
     */
    @Column(nullable = false)
    private int deletedCount;

    /**
     * Timestamp when the deletion occurred
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Additional details about the deletion (e.g., error messages, counts by
     * direction)
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    public AuditLog(String action, UUID userUUID, String recordType, int deletedCount, LocalDateTime timestamp,
            String details) {
        this.action = action;
        this.userUUID = userUUID;
        this.recordType = recordType;
        this.deletedCount = deletedCount;
        this.timestamp = timestamp;
        this.details = details;
    }
}
