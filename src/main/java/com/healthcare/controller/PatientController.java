package com.healthcare.controller;

import com.healthcare.dto.request.UpdatePatientProfileRequest;
import com.healthcare.dto.response.PatientProfileResponse;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@Tag(name = "Patient", description = "Patient management endpoints")
@SecurityRequirement(name = "bearer-auth")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient profile", description = "Retrieve the current patient's profile")
    public ResponseEntity<PatientProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PatientProfileResponse response = patientService.getPatientProfile(currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get patient by ID", description = "Retrieve patient profile by ID (patient can view own, doctor can view assigned patients)")
    public ResponseEntity<PatientProfileResponse> getPatientById(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PatientProfileResponse response = patientService.getPatientById(patientId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update patient profile", description = "Update the current patient's profile information")
    public ResponseEntity<PatientProfileResponse> updateProfile(
            @Valid @RequestBody UpdatePatientProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PatientProfileResponse response = patientService.updatePatientProfile(request, currentUser.getUser());
        return ResponseEntity.ok(response);
    }
}
