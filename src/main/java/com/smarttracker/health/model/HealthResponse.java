package com.smarttracker.health.model;

import java.time.Instant;
import java.util.Map;

public record HealthResponse(
        String status,
        Instant timestamp,
        Map<String, Object> components
) {
    public static HealthResponse up(Map<String, Object> components) {
        return new HealthResponse("UP", Instant.now(), components);
    }

    public static HealthResponse down(Map<String, Object> components) {
        return new HealthResponse("DOWN", Instant.now(), components);
    }
}
