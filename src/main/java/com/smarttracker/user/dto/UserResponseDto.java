package com.smarttracker.user.dto;

import com.smarttracker.user.entity.User;

import java.time.OffsetDateTime;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        OffsetDateTime createdAt
) {
    public static UserResponseDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
