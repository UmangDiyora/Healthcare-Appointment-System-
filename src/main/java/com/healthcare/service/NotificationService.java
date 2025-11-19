package com.healthcare.service;

import com.healthcare.entity.*;
import com.healthcare.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Async
    @Transactional
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            // Create notification record
            Notification notification = Notification.builder()
                    .user(appointment.getPatient().getUser())
                    .notificationType(NotificationType.APPOINTMENT_CONFIRMED)
                    .title("Appointment Confirmed")
                    .message(buildAppointmentConfirmationMessage(appointment))
                    .isRead(false)
                    .isSentEmail(false)
                    .isSentSms(false)
                    .build();

            notification = notificationRepository.save(notification);

            // Send email
            emailService.sendAppointmentConfirmation(appointment);
            notification.setIsSentEmail(true);

            // Send SMS
            smsService.sendAppointmentConfirmation(appointment);
            notification.setIsSentSms(true);

            notification.setSentTime(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Appointment confirmation sent for appointment: {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send appointment confirmation", e);
        }
    }

    @Async
    @Transactional
    public void scheduleReminder(Appointment appointment, int hoursBeforeAppointment) {
        try {
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime()
            );

            LocalDateTime reminderTime = appointmentDateTime.minusHours(hoursBeforeAppointment);

            // Only schedule if reminder time is in the future
            if (reminderTime.isAfter(LocalDateTime.now())) {
                Notification notification = Notification.builder()
                        .user(appointment.getPatient().getUser())
                        .notificationType(NotificationType.APPOINTMENT_REMINDER)
                        .title("Appointment Reminder")
                        .message(buildAppointmentReminderMessage(appointment))
                        .isRead(false)
                        .isSentEmail(false)
                        .isSentSms(false)
                        .scheduledTime(reminderTime)
                        .build();

                notificationRepository.save(notification);

                log.info("Reminder scheduled for appointment: {} at {}", appointment.getId(), reminderTime);
            }

        } catch (Exception e) {
            log.error("Failed to schedule reminder for appointment: {}", appointment.getId(), e);
        }
    }

    @Async
    @Transactional
    public void sendAppointmentCancellation(Appointment appointment, String reason) {
        try {
            Notification notification = Notification.builder()
                    .user(appointment.getPatient().getUser())
                    .notificationType(NotificationType.APPOINTMENT_CANCELLED)
                    .title("Appointment Cancelled")
                    .message(buildAppointmentCancellationMessage(appointment, reason))
                    .isRead(false)
                    .isSentEmail(false)
                    .isSentSms(false)
                    .build();

            notification = notificationRepository.save(notification);

            // Send email
            emailService.sendAppointmentCancellation(appointment, reason);
            notification.setIsSentEmail(true);
            notification.setSentTime(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Cancellation notification sent for appointment: {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send cancellation notification", e);
        }
    }

    @Async
    @Transactional
    public void notifyNewPrescription(Prescription prescription) {
        try {
            Notification notification = Notification.builder()
                    .user(prescription.getPatient().getUser())
                    .notificationType(NotificationType.NEW_PRESCRIPTION)
                    .title("New Prescription")
                    .message(String.format("Dr. %s has prescribed %s. Check your account for details.",
                            prescription.getDoctor().getLastName(),
                            prescription.getMedicationName()))
                    .isRead(false)
                    .isSentEmail(false)
                    .isSentSms(false)
                    .build();

            notification = notificationRepository.save(notification);

            // Send email
            emailService.sendPrescriptionEmail(prescription);
            notification.setIsSentEmail(true);
            notification.setSentTime(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Prescription notification sent for prescription: {}", prescription.getId());

        } catch (Exception e) {
            log.error("Failed to send prescription notification", e);
        }
    }

    @Async
    @Transactional
    public void notifyNewMedicalRecord(MedicalRecord record) {
        try {
            Notification notification = Notification.builder()
                    .user(record.getPatient().getUser())
                    .notificationType(NotificationType.NEW_RECORD)
                    .title("New Medical Record")
                    .message(String.format("A new %s has been added to your medical records by Dr. %s.",
                            record.getRecordType(),
                            record.getDoctor().getLastName()))
                    .isRead(false)
                    .isSentEmail(false)
                    .isSentSms(false)
                    .build();

            notification = notificationRepository.save(notification);

            // Send email
            emailService.sendNewMedicalRecordNotification(
                    record.getPatient().getUser().getEmail(),
                    record.getPatient().getFirstName() + " " + record.getPatient().getLastName(),
                    record.getRecordType().toString()
            );
            notification.setIsSentEmail(true);
            notification.setSentTime(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Medical record notification sent for record: {}", record.getId());

        } catch (Exception e) {
            log.error("Failed to send medical record notification", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user, Boolean unreadOnly) {
        if (unreadOnly != null && unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User currentUser) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalse(currentUser.getId());

        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Helper methods

    private String buildAppointmentConfirmationMessage(Appointment appointment) {
        return String.format("Your appointment with Dr. %s on %s at %s has been confirmed.",
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime());
    }

    private String buildAppointmentReminderMessage(Appointment appointment) {
        return String.format("Reminder: You have an appointment with Dr. %s tomorrow at %s.",
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentTime());
    }

    private String buildAppointmentCancellationMessage(Appointment appointment, String reason) {
        String baseMessage = String.format("Your appointment with Dr. %s on %s at %s has been cancelled.",
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime());

        if (reason != null && !reason.isEmpty()) {
            return baseMessage + " Reason: " + reason;
        }
        return baseMessage;
    }
}
