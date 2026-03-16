package com.profileconnectionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Report extends ProfileConnection {

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    public Report(java.util.UUID initiator, java.util.UUID target, ReportReason reason, String description) {
        super(initiator, target);
        this.reason = reason;
        this.description = description;
        this.status = ReportStatus.PENDING;
    }

    public enum ReportReason {
        HARASSMENT,
        HATE_SPEECH,
        SPAM,
        INAPPROPRIATE_CONTENT,
        MISINFORMATION,
        IMPERSONATION,
        SCAM_FRAUD,
        VIOLENCE,
        SEXUAL_CONTENT,
        OTHER
    }

    public enum ReportStatus {
        PENDING,
        UNDER_REVIEW,
        RESOLVED,
        DISMISSED,
        ACTION_TAKEN
    }
}
