package com.smarttracker.auth.dto;

import com.smarttracker.user.dto.UserResponseDto;

public record AuthResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponseDto user
) {
    public static AuthResponseDto bearer(String token, long expiresInMs, UserResponseDto user) {
        return new AuthResponseDto(token, "Bearer", expiresInMs, user);
    }
}
