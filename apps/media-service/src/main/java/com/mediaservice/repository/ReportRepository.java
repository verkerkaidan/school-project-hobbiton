package com.mediaservice.repository;

import com.mediaservice.model.FileReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<FileReport, UUID> {
    // Retrieve the reports that match the given file identifier
    List<FileReport> getAllByFileId(UUID fileId);

    // Deletes all reports that reference a file, after the file has been updated
    void deleteAllByFileId(UUID fileId);

    // Retrieve all distinct fileIds from reported files
    @Query("SELECT DISTINCT e.fileId FROM FileReport e")
    List<UUID> getDistinctFileIds();
}
