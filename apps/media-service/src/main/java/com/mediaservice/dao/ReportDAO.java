package com.mediaservice.dao;

import com.mediaservice.model.FileReport;
import com.mediaservice.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ReportDAO {
    @Autowired
    private ReportRepository reportRepository;

    public List<FileReport> getFileReportsByFileId(UUID fileId) {
        return reportRepository.getAllByFileId(fileId);
    }

    public void deleteAllReportsByFileId(UUID fileId) {
        reportRepository.deleteAllByFileId(fileId);
    }

    public void saveReport(FileReport report) {
        reportRepository.save(report);
    }

    public List<UUID> getAllDistinctFiles() {
        return reportRepository.getDistinctFileIds();
    }
}
