package com.healthcare.repository;

import com.healthcare.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId " +
           "AND n.isRead = false " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUser(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.scheduledTime IS NOT NULL " +
           "AND n.scheduledTime BETWEEN :startTime AND :endTime " +
           "AND (n.isSentEmail = false OR n.isSentSms = false)")
    List<Notification> findPendingReminders(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    List<Notification> findByNotificationType(Notification.NotificationType type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadNotificationsByUser(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.relatedEntityId = :entityId " +
           "AND n.notificationType = :type")
    List<Notification> findByRelatedEntityIdAndType(@Param("entityId") Long entityId,
                                                    @Param("type") Notification.NotificationType type);

    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
