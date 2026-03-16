package com.profileconnectionservice.controller.dto;

import com.profileconnectionservice.model.Report;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    private UUID initiatorId;
    private UUID targetId;
    private Report.ReportReason reason;
    private String description;
}
