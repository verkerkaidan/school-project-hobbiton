package com.profileconnectionservice.repository;

import com.profileconnectionservice.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    // Delete all reports created by this profile (as initiator/reporter)
    @Modifying
    @Transactional
    @Query("DELETE FROM Report r WHERE r.initiator = :initiator")
    int deleteByInitiator(UUID initiator);

    // Delete all reports about this profile (as target/reported profile)
    @Modifying
    @Transactional
    @Query("DELETE FROM Report r WHERE r.target = :target")
    int deleteByTarget(UUID target);
}
