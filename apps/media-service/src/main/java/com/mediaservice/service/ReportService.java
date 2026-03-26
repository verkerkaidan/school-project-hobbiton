package com.mediaservice.service;

import com.mediaservice.dao.ReportDAO;
import com.mediaservice.dto.ContentFlagUpdateDTO;
import com.mediaservice.dto.ReportUpdatedDTO;
import com.mediaservice.model.FileReport;
import com.mediaservice.model.MediaFile;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReportService {
    @Autowired
    private MediaService mediaService;

    @Autowired
    private ReportDAO reportDAO;

    private final RabbitTemplate rabbitTemplate;

    public ReportService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Creating a new report using a userId and the identifier of the instance that was reported
     *
     * @param userId, identifier of the user that reported
     * @param instanceId, identifier of the instance that was reported
     * @return reportId, identifier of the report
     */
    public UUID createNewReport(UUID userId, UUID instanceId) {
        MediaFile mediaFile = mediaService.getMediaFile(instanceId);
        FileReport report = new FileReport(userId, mediaFile.getFileId());
        reportDAO.saveReport(report);

        return report.getReportId();
    }

    /**
     * After the contentFlags of files are updated, this function deletes the referring reports
     * calls for the observers to be notified
     *
     * @param dtoList, <String fileId, ContentFlag contentFlag, String note>[]
     */
    public void updateReports(List<ContentFlagUpdateDTO> dtoList) {
        dtoList.forEach((dto) -> {
            List<UUID> relevantUsers = reportDAO.getFileReportsByFileId(dto.fileId)
                    .stream().map(
                            (report) -> report.getUserID())
                    .toList();
            this.publishReportUpdated(
                    new ReportUpdatedDTO(relevantUsers, dto.note)
            );
            reportDAO.deleteAllReportsByFileId(dto.fileId);
        });
    }

    /**
     * Sends out the events of reports being updated
     *
     * @param event, ReportUpdatedDTO with userId's and the note given with the file update
     */
    void publishReportUpdated(ReportUpdatedDTO event) {
        rabbitTemplate.convertAndSend(
                "HobbyApp_reportEvents",
                "report.updated",
                event
        );
    }
}
