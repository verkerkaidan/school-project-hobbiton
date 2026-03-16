package com.profileconnectionservice.repository;

import com.profileconnectionservice.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByTarget(UUID targetId);

    List<Report> findByInitiator(UUID initiatorId);

    List<Report> findByStatus(Report.ReportStatus status);

    List<Report> findByTargetAndInitiator(UUID targetId, UUID initiatorId);

    Optional<Report> findByIdAndInitiator(UUID reportId, UUID initiatorId);
}
