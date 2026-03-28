package com.mediaservice.dto;

import java.util.List;
import java.util.UUID;

public class ReportUpdatedDTO {
    public List<UUID> userId;
    public String note;

    public ReportUpdatedDTO(List<UUID> userId, String note) {
        this.userId = userId;
        this.note = note;
    }

    public ReportUpdatedDTO() {
    }
}
