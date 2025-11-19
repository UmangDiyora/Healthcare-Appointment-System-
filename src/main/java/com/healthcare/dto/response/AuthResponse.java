package com.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String userType;
    private Long expiresIn; // in seconds

    public static AuthResponse of(String token, Long userId, String email, String userType, Long expiresIn) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .userType(userType)
                .expiresIn(expiresIn)
                .build();
    }
}
