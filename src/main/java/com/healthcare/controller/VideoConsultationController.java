package com.healthcare.controller;

import com.healthcare.dto.response.ApiResponse;
import com.healthcare.dto.response.VideoRoomResponse;
import com.healthcare.dto.response.VideoTokenResponse;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.VideoConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@Tag(name = "Video Consultation", description = "Video consultation endpoints using Twilio")
@SecurityRequirement(name = "bearer-auth")
public class VideoConsultationController {

    private final VideoConsultationService videoConsultationService;

    @PostMapping("/appointments/{appointmentId}/room")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Create video room", description = "Create a Twilio video room for an appointment")
    public ResponseEntity<VideoRoomResponse> createRoom(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        VideoRoomResponse response = videoConsultationService.createRoom(appointmentId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments/{appointmentId}/token")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get video access token", description = "Generate a Twilio access token for joining the video consultation")
    public ResponseEntity<VideoTokenResponse> getVideoToken(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        VideoTokenResponse response = videoConsultationService.generateAccessToken(appointmentId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/appointments/{appointmentId}/end")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "End consultation", description = "End the video consultation and mark appointment as completed (doctor only)")
    public ResponseEntity<ApiResponse> endConsultation(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        videoConsultationService.endConsultation(appointmentId, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Video consultation ended successfully")
                .build());
    }

    @GetMapping("/appointments/{appointmentId}/status")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get room status", description = "Get the current status of the video room")
    public ResponseEntity<VideoRoomResponse> getRoomStatus(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        VideoRoomResponse response = videoConsultationService.getRoomStatus(appointmentId, currentUser.getUser());
        return ResponseEntity.ok(response);
    }
}
