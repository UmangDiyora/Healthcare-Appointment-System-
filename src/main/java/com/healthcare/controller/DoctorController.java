package com.healthcare.controller;

import com.healthcare.dto.request.SetAvailabilityRequest;
import com.healthcare.dto.request.UpdateDoctorProfileRequest;
import com.healthcare.dto.response.AvailabilityResponse;
import com.healthcare.dto.response.DoctorProfileResponse;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.DoctorService;
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
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@Tag(name = "Doctor", description = "Doctor management endpoints")
@SecurityRequirement(name = "bearer-auth")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get doctor profile", description = "Retrieve the current doctor's profile")
    public ResponseEntity<DoctorProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DoctorProfileResponse response = doctorService.getDoctorProfile(currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve doctor profile by ID")
    public ResponseEntity<DoctorProfileResponse> getDoctorById(@PathVariable Long doctorId) {
        DoctorProfileResponse response = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update doctor profile", description = "Update the current doctor's profile information")
    public ResponseEntity<DoctorProfileResponse> updateProfile(
            @Valid @RequestBody UpdateDoctorProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DoctorProfileResponse response = doctorService.updateDoctorProfile(request, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Set availability", description = "Set doctor's availability schedule")
    public ResponseEntity<AvailabilityResponse> setAvailability(
            @Valid @RequestBody SetAvailabilityRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AvailabilityResponse response = doctorService.setAvailability(request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{doctorId}/availability")
    @Operation(summary = "Get doctor availability", description = "Retrieve doctor's availability schedule")
    public ResponseEntity<List<AvailabilityResponse>> getDoctorAvailability(@PathVariable Long doctorId) {
        List<AvailabilityResponse> availabilities = doctorService.getDoctorAvailability(doctorId);
        return ResponseEntity.ok(availabilities);
    }

    @DeleteMapping("/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete availability", description = "Delete a specific availability slot")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable Long availabilityId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        doctorService.deleteAvailability(availabilityId, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctors", description = "Search doctors by specialization")
    public ResponseEntity<List<DoctorProfileResponse>> searchDoctors(
            @RequestParam(required = false) String specialization) {
        List<DoctorProfileResponse> doctors = doctorService.searchDoctors(specialization);
        return ResponseEntity.ok(doctors);
    }
}
