package com.healthcare.service;

import com.healthcare.entity.Appointment;
import com.healthcare.entity.Notification;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        if (twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()) {
            log.warn("Twilio phone number not configured. SMS not sent.");
            return;
        }

        try {
            String patientPhone = appointment.getPatient().getUser().getPhoneNumber();
            if (patientPhone == null || patientPhone.isEmpty()) {
                log.warn("Patient phone number not available for appointment: {}", appointment.getId());
                return;
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            String messageBody = String.format(
                    "Reminder: You have an appointment tomorrow at %s with Dr. %s. " +
                            "Type: %s. Reply CANCEL to cancel.",
                    appointment.getAppointmentTime().format(timeFormatter),
                    appointment.getDoctor().getLastName(),
                    appointment.getAppointmentType()
            );

            Message.creator(
                    new PhoneNumber(patientPhone),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            log.info("SMS reminder sent to: {} for appointment: {}", patientPhone, appointment.getId());

        } catch (ApiException e) {
            log.error("Twilio API error sending SMS for appointment: {}", appointment.getId(), e);
        } catch (Exception e) {
            log.error("Failed to send SMS reminder for appointment: {}", appointment.getId(), e);
        }
    }

    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        if (twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()) {
            log.warn("Twilio phone number not configured. SMS not sent.");
            return;
        }

        try {
            String patientPhone = appointment.getPatient().getUser().getPhoneNumber();
            if (patientPhone == null || patientPhone.isEmpty()) {
                return;
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            String messageBody = String.format(
                    "Appointment confirmed for %s at %s with Dr. %s (%s). Confirmation #%d",
                    appointment.getAppointmentDate().format(dateFormatter),
                    appointment.getAppointmentTime().format(timeFormatter),
                    appointment.getDoctor().getLastName(),
                    appointment.getAppointmentType(),
                    appointment.getId()
            );

            Message.creator(
                    new PhoneNumber(patientPhone),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            log.info("SMS confirmation sent to: {} for appointment: {}", patientPhone, appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send SMS confirmation for appointment: {}", appointment.getId(), e);
        }
    }

    @Async
    public void sendCustomNotification(Notification notification) {
        if (twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()) {
            log.warn("Twilio phone number not configured. SMS not sent.");
            return;
        }

        try {
            String userPhone = notification.getUser().getPhoneNumber();
            if (userPhone == null || userPhone.isEmpty()) {
                log.warn("User phone number not available for notification: {}", notification.getId());
                return;
            }

            Message.creator(
                    new PhoneNumber(userPhone),
                    new PhoneNumber(twilioPhoneNumber),
                    notification.getMessage()
            ).create();

            log.info("SMS notification sent to: {} for notification: {}", userPhone, notification.getId());

        } catch (Exception e) {
            log.error("Failed to send SMS notification: {}", notification.getId(), e);
        }
    }
}
