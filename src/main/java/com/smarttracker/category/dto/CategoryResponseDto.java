package com.smarttracker.category.dto;

import com.smarttracker.category.entity.Category;

import java.time.OffsetDateTime;

public record CategoryResponseDto(
        Long id,
        String name,
        boolean isSystem,
        OffsetDateTime createdAt
) {
    public static CategoryResponseDto fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.isSystem(),
                category.getCreatedAt()
        );
    }
}
