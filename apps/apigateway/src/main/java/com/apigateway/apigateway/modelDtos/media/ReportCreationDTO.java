package com.apigateway.apigateway.modelDtos.media;

import java.util.UUID;

public class ReportCreationDTO {
    public UUID report_id;
    public String status;
    public String reason;

    public ReportCreationDTO() {
    }

    public ReportCreationDTO(UUID report_id, String reason, String status) {
        this.report_id = report_id;
        this.reason = reason;
        this.status = status;
    }
}
