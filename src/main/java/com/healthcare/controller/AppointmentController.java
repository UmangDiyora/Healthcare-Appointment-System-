package com.healthcare.controller;

import com.healthcare.dto.request.CancelAppointmentRequest;
import com.healthcare.dto.request.CreateAppointmentRequest;
import com.healthcare.dto.request.RescheduleAppointmentRequest;
import com.healthcare.dto.response.AppointmentResponse;
import com.healthcare.dto.response.DoctorAvailabilitySlotResponse;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment management endpoints")
@SecurityRequirement(name = "bearer-auth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book appointment", description = "Create a new appointment with a doctor")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AppointmentResponse response = appointmentService.bookAppointment(request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/availability")
    @Operation(summary = "Get available slots", description = "Get available appointment slots for a doctor on a specific date")
    public ResponseEntity<DoctorAvailabilitySlotResponse> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DoctorAvailabilitySlotResponse response = appointmentService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get appointment", description = "Get appointment details by ID")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AppointmentResponse response = appointmentService.getAppointmentById(appointmentId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient appointments", description = "Get all appointments for the current patient")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(currentUser.getUser());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/my-appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get doctor appointments", description = "Get appointments for the current doctor, optionally filtered by date")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(currentUser.getUser(), date);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Cancel appointment", description = "Cancel an existing appointment")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, request, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Reschedule appointment", description = "Reschedule an existing appointment to a new date/time")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody RescheduleAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(appointmentId, request, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Complete appointment", description = "Mark an appointment as completed (doctor only)")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AppointmentResponse response = appointmentService.completeAppointment(appointmentId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }
}
