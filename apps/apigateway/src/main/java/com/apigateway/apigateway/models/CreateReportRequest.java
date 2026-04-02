package com.apigateway.apigateway.models;

import java.util.UUID;

public class CreateReportRequest {
    private UUID initiatorId;
    private UUID targetId;
    private String reason;
    private String description;

    public CreateReportRequest() {
    }

    public CreateReportRequest(UUID initiatorId, UUID targetId, String reason, String description) {
        this.initiatorId = initiatorId;
        this.targetId = targetId;
        this.reason = reason;
        this.description = description;
    }

    public UUID getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(UUID initiatorId) {
        this.initiatorId = initiatorId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
