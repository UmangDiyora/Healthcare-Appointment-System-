package com.healthcare.service;

import com.healthcare.entity.Appointment;
import com.healthcare.entity.Notification;
import com.healthcare.entity.Prescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@healthcare.com}")
    private String fromEmail;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            String patientEmail = appointment.getPatient().getUser().getEmail();
            String subject = "Appointment Confirmed - Dr. " + appointment.getDoctor().getLastName();

            String body = buildAppointmentConfirmationEmail(appointment);

            sendHtmlEmail(patientEmail, subject, body);

            log.info("Appointment confirmation email sent to: {}", patientEmail);

        } catch (Exception e) {
            log.error("Failed to send appointment confirmation email", e);
        }
    }

    @Async
    public void sendAppointmentReminder(Notification notification) {
        try {
            // Extract email from notification user
            String email = notification.getUser().getEmail();
            String subject = notification.getTitle();
            String body = notification.getMessage();

            sendSimpleEmail(email, subject, body);

            log.info("Appointment reminder sent to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send appointment reminder", e);
        }
    }

    @Async
    public void sendAppointmentCancellation(Appointment appointment, String reason) {
        try {
            String patientEmail = appointment.getPatient().getUser().getEmail();
            String subject = "Appointment Cancelled";

            String body = buildAppointmentCancellationEmail(appointment, reason);

            sendHtmlEmail(patientEmail, subject, body);

            log.info("Appointment cancellation email sent to: {}", patientEmail);

        } catch (Exception e) {
            log.error("Failed to send appointment cancellation email", e);
        }
    }

    @Async
    public void sendPrescriptionEmail(Prescription prescription) {
        try {
            String patientEmail = prescription.getPatient().getUser().getEmail();
            String subject = "New Prescription from Dr. " + prescription.getDoctor().getLastName();

            String body = buildPrescriptionEmail(prescription);

            sendHtmlEmail(patientEmail, subject, body);

            log.info("Prescription email sent to: {}", patientEmail);

        } catch (Exception e) {
            log.error("Failed to send prescription email", e);
        }
    }

    @Async
    public void sendNewMedicalRecordNotification(String patientEmail, String patientName, String recordType) {
        try {
            String subject = "New Medical Record Added";

            String body = String.format("""
                    <html>
                    <body>
                        <h2>New Medical Record</h2>
                        <p>Dear %s,</p>
                        <p>A new medical record has been added to your account:</p>
                        <p><strong>Record Type:</strong> %s</p>
                        <p>You can view this record by logging into your account.</p>
                        <p><a href="%s/patient/medical-records">View Medical Records</a></p>
                        <br>
                        <p>Best regards,<br>Healthcare Team</p>
                    </body>
                    </html>
                    """, patientName, recordType, baseUrl);

            sendHtmlEmail(patientEmail, subject, body);

            log.info("Medical record notification sent to: {}", patientEmail);

        } catch (Exception e) {
            log.error("Failed to send medical record notification", e);
        }
    }

    // Helper methods

    private void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    private String buildAppointmentConfirmationEmail(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String videoLink = appointment.getAppointmentType().name().equals("VIDEO")
                ? String.format("<p><strong>Video Link:</strong> <a href=\"%s/video/appointments/%d\">Join Video Consultation</a></p>",
                baseUrl, appointment.getId())
                : "";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2c3e50;">Appointment Confirmed</h2>
                        <p>Dear %s,</p>
                        <p>Your appointment has been confirmed with the following details:</p>

                        <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Time:</strong> %s</p>
                            <p><strong>Doctor:</strong> Dr. %s %s</p>
                            <p><strong>Specialization:</strong> %s</p>
                            <p><strong>Type:</strong> %s</p>
                            %s
                        </div>

                        <p style="margin-top: 20px;">
                            %s
                        </p>

                        <p style="margin-top: 20px;">To cancel or reschedule, please contact us at least 24 hours in advance.</p>

                        <p style="margin-top: 30px;">Best regards,<br>
                        <strong>Healthcare Team</strong></p>
                    </div>
                </body>
                </html>
                """,
                appointment.getPatient().getFirstName(),
                appointment.getAppointmentDate().format(dateFormatter),
                appointment.getAppointmentTime().format(timeFormatter),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getDoctor().getSpecialization(),
                appointment.getAppointmentType(),
                videoLink,
                appointment.getAppointmentType().name().equals("VIDEO")
                        ? "The video link will be available 10 minutes before your appointment time."
                        : "Please arrive 10 minutes early for check-in."
        );
    }

    private String buildAppointmentCancellationEmail(Appointment appointment, String reason) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String reasonText = reason != null && !reason.isEmpty()
                ? String.format("<p><strong>Reason:</strong> %s</p>", reason)
                : "";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #e74c3c;">Appointment Cancelled</h2>
                        <p>Dear %s,</p>
                        <p>Your appointment has been cancelled:</p>

                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Time:</strong> %s</p>
                            <p><strong>Doctor:</strong> Dr. %s %s</p>
                            %s
                        </div>

                        <p>If you would like to reschedule, please book a new appointment at your convenience.</p>

                        <p style="margin-top: 30px;">Best regards,<br>
                        <strong>Healthcare Team</strong></p>
                    </div>
                </body>
                </html>
                """,
                appointment.getPatient().getFirstName(),
                appointment.getAppointmentDate().format(dateFormatter),
                appointment.getAppointmentTime().format(timeFormatter),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                reasonText
        );
    }

    private String buildPrescriptionEmail(Prescription prescription) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #27ae60;">New Prescription</h2>
                        <p>Dear %s,</p>
                        <p>Dr. %s has prescribed the following medication:</p>

                        <div style="background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Medication:</strong> %s</p>
                            <p><strong>Dosage:</strong> %s</p>
                            <p><strong>Frequency:</strong> %s</p>
                            <p><strong>Duration:</strong> %s</p>
                            <p><strong>Instructions:</strong> %s</p>
                            <p><strong>Refills:</strong> %d</p>
                            <p><strong>Date Prescribed:</strong> %s</p>
                        </div>

                        <p style="background-color: #fff3cd; padding: 10px; border-radius: 5px;">
                            <strong>Important:</strong> Please follow the instructions carefully. Contact your doctor if you experience any side effects.
                        </p>

                        <p style="margin-top: 30px;">Best regards,<br>
                        <strong>Healthcare Team</strong></p>
                    </div>
                </body>
                </html>
                """,
                prescription.getPatient().getFirstName(),
                prescription.getDoctor().getLastName(),
                prescription.getMedicationName(),
                prescription.getDosage(),
                prescription.getFrequency(),
                prescription.getDuration() != null ? prescription.getDuration() : "As directed",
                prescription.getInstructions() != null ? prescription.getInstructions() : "Take as prescribed",
                prescription.getRefills(),
                prescription.getPrescribedDate().format(dateFormatter)
        );
    }
}
