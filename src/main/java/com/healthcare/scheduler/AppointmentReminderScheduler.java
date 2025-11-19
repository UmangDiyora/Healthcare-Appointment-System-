package com.healthcare.scheduler;

import com.healthcare.entity.Appointment;
import com.healthcare.entity.Notification;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.NotificationRepository;
import com.healthcare.service.EmailService;
import com.healthcare.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    /**
     * Runs every hour to send scheduled appointment reminders
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void sendScheduledReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);

            // Find notifications scheduled between now and one hour from now
            List<Notification> pendingReminders = notificationRepository
                    .findByScheduledTimeBetweenAndIsSentEmailFalse(now, oneHourLater);

            log.info("Found {} pending reminders to send", pendingReminders.size());

            for (Notification notification : pendingReminders) {
                try {
                    // Send email reminder
                    emailService.sendAppointmentReminder(notification);
                    notification.setIsSentEmail(true);

                    // Send SMS reminder
                    smsService.sendCustomNotification(notification);
                    notification.setIsSentSms(true);

                    notification.setSentTime(LocalDateTime.now());
                    notificationRepository.save(notification);

                    log.info("Reminder sent for notification: {}", notification.getId());

                } catch (Exception e) {
                    log.error("Failed to send reminder for notification: {}", notification.getId(), e);
                }
            }

            log.info("Completed sending {} reminders", pendingReminders.size());

        } catch (Exception e) {
            log.error("Error in reminder scheduler", e);
        }
    }

    /**
     * Runs every day at 8 AM to send reminders for appointments happening tomorrow
     */
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8:00 AM
    @Transactional
    public void sendDailyAppointmentReminders() {
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            // Find all scheduled appointments for tomorrow
            List<Appointment> tomorrowAppointments = appointmentRepository
                    .findAppointmentsForDate(tomorrow);

            log.info("Found {} appointments for tomorrow ({})", tomorrowAppointments.size(), tomorrow);

            for (Appointment appointment : tomorrowAppointments) {
                try {
                    // Send SMS reminder
                    smsService.sendAppointmentReminder(appointment);

                    log.info("Daily reminder sent for appointment: {}", appointment.getId());

                } catch (Exception e) {
                    log.error("Failed to send daily reminder for appointment: {}", appointment.getId(), e);
                }
            }

            log.info("Completed sending daily reminders");

        } catch (Exception e) {
            log.error("Error in daily reminder scheduler", e);
        }
    }

    /**
     * Runs every 6 hours to clean up old read notifications (older than 30 days)
     */
    @Scheduled(cron = "0 0 */6 * * *") // Every 6 hours
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

            List<Notification> oldNotifications = notificationRepository
                    .findByIsReadTrueAndCreatedAtBefore(cutoffDate);

            if (!oldNotifications.isEmpty()) {
                notificationRepository.deleteAll(oldNotifications);
                log.info("Cleaned up {} old notifications", oldNotifications.size());
            }

        } catch (Exception e) {
            log.error("Error in notification cleanup scheduler", e);
        }
    }
}
