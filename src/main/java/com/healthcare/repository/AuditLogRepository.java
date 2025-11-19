package com.healthcare.repository;

import com.healthcare.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity
 * HIPAA Requirement: Immutable audit logs for tracking all PHI access
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByAction(AuditLog.Action action);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId " +
           "AND al.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY al.timestamp DESC")
    List<AuditLog> findByUserIdAndDateRange(@Param("userId") Long userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType " +
           "AND al.entityId = :entityId " +
           "ORDER BY al.timestamp DESC")
    List<AuditLog> findAccessHistoryForEntity(@Param("entityType") String entityType,
                                             @Param("entityId") Long entityId);

    @Query("SELECT al FROM AuditLog al WHERE al.action = :action " +
           "AND al.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByActionAndDateRange(@Param("action") AuditLog.Action action,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress " +
           "ORDER BY al.timestamp DESC")
    List<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress);

    @Query("SELECT al FROM AuditLog al WHERE al.action = 'LOGIN_FAILED' " +
           "AND al.userEmail = :email " +
           "AND al.timestamp > :since")
    List<AuditLog> findFailedLoginAttempts(@Param("email") String email,
                                          @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // For HIPAA compliance - find all access to specific PHI
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = 'PATIENT' " +
           "AND al.entityId = :patientId " +
           "AND al.action IN ('VIEW', 'UPDATE', 'EXPORT', 'DOWNLOAD') " +
           "ORDER BY al.timestamp DESC")
    List<AuditLog> findPhiAccessHistory(@Param("patientId") Long patientId);
}
