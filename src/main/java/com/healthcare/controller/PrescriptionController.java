package com.healthcare.controller;

import com.healthcare.dto.request.CreatePrescriptionRequest;
import com.healthcare.dto.response.PrescriptionResponse;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Prescription management endpoints")
@SecurityRequirement(name = "bearer-auth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create prescription", description = "Create a new prescription for a patient (doctor only)")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PrescriptionResponse response = prescriptionService.createPrescription(request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{prescriptionId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get prescription", description = "Get prescription details by ID")
    public ResponseEntity<PrescriptionResponse> getPrescription(
            @PathVariable Long prescriptionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PrescriptionResponse response = prescriptionService.getPrescription(prescriptionId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get patient prescriptions", description = "Get all prescriptions for a specific patient")
    public ResponseEntity<List<PrescriptionResponse>> getPatientPrescriptions(
            @PathVariable Long patientId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<PrescriptionResponse> prescriptions = prescriptionService.getPatientPrescriptions(
                patientId, currentUser.getUser(), activeOnly);
        return ResponseEntity.ok(prescriptions);
    }

    @PutMapping("/{prescriptionId}/deactivate")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Deactivate prescription", description = "Mark a prescription as inactive (doctor only)")
    public ResponseEntity<PrescriptionResponse> deactivatePrescription(
            @PathVariable Long prescriptionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PrescriptionResponse response = prescriptionService.deactivatePrescription(prescriptionId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }
}
