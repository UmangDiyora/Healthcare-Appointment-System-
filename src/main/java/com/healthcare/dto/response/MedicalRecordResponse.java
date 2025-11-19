package com.healthcare.dto.response;

import com.healthcare.entity.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long appointmentId;
    private RecordType recordType;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
}
