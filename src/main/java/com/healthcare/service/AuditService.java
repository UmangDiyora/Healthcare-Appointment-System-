package com.healthcare.service;

import com.healthcare.entity.AuditLog;
import com.healthcare.entity.User;
import com.healthcare.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Audit Service for HIPAA compliance
 * Logs all access to sensitive data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an audit entry asynchronously
     */
    @Async
    @Transactional
    public void log(User user, String action, String entityType, Long entityId) {
        log(user, action, entityType, entityId, null, null, null);
    }

    /**
     * Log an audit entry with additional details
     */
    @Async
    @Transactional
    public void log(User user,
                   String action,
                   String entityType,
                   Long entityId,
                   String ipAddress,
                   String userAgent,
                   String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Audit log created: User={}, Action={}, Entity={}:{}",
                    user.getId(), action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log authentication attempt
     */
    @Async
    @Transactional
    public void logAuthentication(String email, boolean success, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED")
                    .entityType("AUTH")
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(String.format("Email: %s", email))
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Authentication audit log created: Email={}, Success={}", email, success);
        } catch (Exception e) {
            log.error("Failed to create authentication audit log", e);
        }
    }

    /**
     * Log data export for compliance
     */
    @Async
    @Transactional
    public void logDataExport(User user, String entityType, String exportFormat, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action("EXPORT")
                    .entityType(entityType)
                    .ipAddress(ipAddress)
                    .details(String.format("Format: %s", exportFormat))
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.info("Data export logged: User={}, EntityType={}, Format={}",
                    user.getId(), entityType, exportFormat);
        } catch (Exception e) {
            log.error("Failed to create data export audit log", e);
        }
    }
}
