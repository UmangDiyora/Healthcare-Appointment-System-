package com.healthcare.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Notification entity
 * Manages system notifications to users
 */
@Entity
@Table(name = "notifications",
       indexes = {
           @Index(name = "idx_notification_user_read", columnList = "user_id,is_read"),
           @Index(name = "idx_notification_scheduled", columnList = "scheduled_time")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_sent_email", nullable = false)
    @Builder.Default
    private Boolean isSentEmail = false;

    @Column(name = "is_sent_sms", nullable = false)
    @Builder.Default
    private Boolean isSentSms = false;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        APPOINTMENT_REMINDER,
        APPOINTMENT_CONFIRMED,
        APPOINTMENT_CANCELLED,
        APPOINTMENT_RESCHEDULED,
        NEW_PRESCRIPTION,
        NEW_MEDICAL_RECORD,
        REVIEW_REQUEST,
        SYSTEM_ALERT
    }
}
