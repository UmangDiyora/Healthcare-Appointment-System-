package com.healthcare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit Log entity - HIPAA Requirement
 * Immutable logs of all PHI access and modifications
 * Must be retained for minimum 7 years
 */
@Entity
@Table(name = "audit_logs",
       indexes = {
           @Index(name = "idx_audit_user_timestamp", columnList = "user_id,timestamp"),
           @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
           @Index(name = "idx_audit_timestamp", columnList = "timestamp")
       })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Action action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "response_status")
    private Integer responseStatus;

    public enum Action {
        VIEW,
        CREATE,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        EXPORT,
        DOWNLOAD,
        UPLOAD,
        PASSWORD_RESET,
        EMAIL_VERIFICATION,
        ACCESS_DENIED
    }

    // Make the entity immutable - no setters
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
