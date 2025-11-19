package com.healthcare.dto.request;

import com.healthcare.entity.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalRecordRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long appointmentId;

    @NotNull(message = "Record type is required")
    private RecordType recordType;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;
}
