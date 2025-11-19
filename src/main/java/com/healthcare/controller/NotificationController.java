package com.healthcare.controller;

import com.healthcare.dto.response.ApiResponse;
import com.healthcare.entity.Notification;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearer-auth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get notifications", description = "Get all notifications for the current user")
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Notification> notifications = notificationService.getUserNotifications(
                currentUser.getUser(), unreadOnly);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAsRead(notificationId, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Notification marked as read")
                .build());
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for the current user")
    public ResponseEntity<ApiResponse> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("All notifications marked as read")
                .build());
    }
}
