package com.mediaservice.dto;

import java.util.List;
import java.util.UUID;

public class ReportUpdatedDTO {
    public List<UUID> userIds;
    public String note;

    public ReportUpdatedDTO(List<UUID> userIds, String note) {
        this.userIds = userIds;
        this.note = note;
    }

    public ReportUpdatedDTO() {
    }
}
