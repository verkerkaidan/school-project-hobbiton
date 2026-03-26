package com.profileconnectionservice.service;

import com.profileconnectionservice.model.AuditLog;
import com.profileconnectionservice.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log a deletion operation for GDPR compliance
     * 
     * @param userUUID     the user whose data was deleted
     * @param recordType   the type of records deleted (BLOCK, FOLLOW, REPORT)
     * @param deletedCount the number of records deleted
     * @param details      additional details about the deletion
     * @return the created AuditLog entry
     */
    public AuditLog logDeletion(UUID userUUID, String recordType, int deletedCount, String details) {
        log.debug("Creating audit log for deletion: userUUID={}, recordType={}, deletedCount={}",
                userUUID, recordType, deletedCount);

        AuditLog auditLog = new AuditLog(
                "DELETE_USER_" + recordType,
                userUUID,
                recordType,
                deletedCount,
                LocalDateTime.now(),
                details);

        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Log a deletion operation with no additional details
     */
    public AuditLog logDeletion(UUID userUUID, String recordType, int deletedCount) {
        return logDeletion(userUUID, recordType, deletedCount, null);
    }
}
