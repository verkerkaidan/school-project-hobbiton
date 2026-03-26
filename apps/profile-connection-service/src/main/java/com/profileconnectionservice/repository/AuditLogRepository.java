package com.profileconnectionservice.repository;

import com.profileconnectionservice.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find all audit log entries for a specific user
     */
    List<AuditLog> findByUserUUID(UUID userUUID);

    /**
     * Find all audit log entries for a specific action
     */
    List<AuditLog> findByAction(String action);

    /**
     * Find all audit log entries for a specific record type
     */
    List<AuditLog> findByRecordType(String recordType);
}
