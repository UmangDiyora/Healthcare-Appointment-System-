package com.healthcare.controller;

import com.healthcare.dto.request.CreateMedicalRecordRequest;
import com.healthcare.dto.response.MedicalRecordResponse;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Records", description = "Medical record management endpoints")
@SecurityRequirement(name = "bearer-auth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Upload medical record", description = "Upload a medical record file with metadata (doctor only)")
    public ResponseEntity<MedicalRecordResponse> uploadMedicalRecord(
            @RequestPart("file") MultipartFile file,
            @RequestPart("patientId") Long patientId,
            @RequestPart("recordType") String recordType,
            @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "appointmentId", required = false) Long appointmentId,
            @RequestPart("recordDate") String recordDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        CreateMedicalRecordRequest request = CreateMedicalRecordRequest.builder()
                .patientId(patientId)
                .appointmentId(appointmentId)
                .recordType(com.healthcare.entity.RecordType.valueOf(recordType))
                .title(title)
                .description(description)
                .recordDate(java.time.LocalDate.parse(recordDate))
                .build();

        MedicalRecordResponse response = medicalRecordService.uploadMedicalRecord(file, request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get medical record", description = "Get medical record details by ID")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MedicalRecordResponse response = medicalRecordService.getMedicalRecord(recordId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recordId}/download")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Download medical record", description = "Download the medical record file")
    public ResponseEntity<byte[]> downloadMedicalRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        byte[] fileData = medicalRecordService.downloadMedicalRecord(recordId, currentUser.getUser());
        MedicalRecordResponse record = medicalRecordService.getMedicalRecord(recordId, currentUser.getUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(record.getFileType()));
        headers.setContentDispositionFormData("attachment", record.getTitle());
        headers.setContentLength(fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get patient medical records", description = "Get all medical records for a specific patient")
    public ResponseEntity<List<MedicalRecordResponse>> getPatientMedicalRecords(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<MedicalRecordResponse> records = medicalRecordService.getPatientMedicalRecords(patientId, currentUser.getUser());
        return ResponseEntity.ok(records);
    }

    @DeleteMapping("/{recordId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete medical record", description = "Delete a medical record (doctor only)")
    public ResponseEntity<Void> deleteMedicalRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        medicalRecordService.deleteMedicalRecord(recordId, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }
}
