package com.healthcare.dto.response;

import com.healthcare.entity.AppointmentStatus;
import com.healthcare.entity.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private Integer duration;
    private AppointmentStatus status;
    private AppointmentType appointmentType;
    private String reason;
    private String symptoms;
    private String diagnosis;
    private String prescription;
    private String notes;
    private String twilioRoomSid;
    private String cancellationReason;
    private LocalDateTime createdAt;
}
