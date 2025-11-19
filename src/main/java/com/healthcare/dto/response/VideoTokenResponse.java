package com.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoTokenResponse {
    private String token;
    private String roomSid;
    private String roomName;
    private String identity;
    private LocalDateTime expiresAt;
    private String participantRole;
}
