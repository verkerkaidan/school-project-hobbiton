package com.mediaservice.service;

import com.mediaservice.dao.ReportDAO;
import com.mediaservice.dto.ContentFlagUpdateDTO;
import com.mediaservice.dto.ReportUpdatedDTO;
import com.mediaservice.model.FileReport;
import com.mediaservice.model.MediaFile;
import com.mediaservice.model.MediaInstance;
import com.mediaservice.model.ReportedFile;
import com.mediaservice.repository.ReportedFileRepository;
import jakarta.transaction.Transactional;
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

    @Autowired
    private ReportedFileRepository reportedFileRepository;

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
    @Transactional
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

    /**
     * Saves the hash of a deleted file, so it can't be uploaded in the future
     *
     * @param fileId, unique identifier of the file that was deleted
     */
    public void createNewBannedFile(UUID fileId) {
        MediaFile mediaFile = mediaService.getMediaFileByFileId(fileId);
        System.out.println("[REPORTSERVICE createNewBannedFile] mediaFile: " + mediaFile.toString());
        ReportedFile reportedFile = new ReportedFile(mediaFile.getHash());
        System.out.println("[REPORTSERVICE createNewBannedFile] mediaFile: " + reportedFile.getFileId());
        reportedFileRepository.save(reportedFile);
    }

    /**
     * Retrieve all unique fileId's from the FileReports
     * Retrieve MediaFiles accordingly
     *
     * @return MediaFile[]
     */
    public List<MediaFile> getAllDistinctReports() {
        List<UUID> fileIds = reportDAO.getAllDistinctFiles();
        return fileIds.stream().map(
                (id) -> mediaService.getMediaFileByFileId(id)
        ).toList();
    }
}
