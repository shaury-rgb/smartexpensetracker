package com.smarttracker.health.controller;

import com.smarttracker.health.model.HealthResponse;
import com.smarttracker.health.service.DatabaseHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DatabaseHealthService databaseHealthService;

    public HealthController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> checkHealth() {
        Map<String, Object> components = new LinkedHashMap<>();

        // Application status
        components.put("application", Map.of(
                "status", "UP",
                "name", "smart-expense-tracker"
        ));

        // Database connectivity check
        Map<String, Object> dbHealth = databaseHealthService.checkHealth();
        components.put("database", dbHealth);

        boolean isDbHealthy = "UP".equals(dbHealth.get("status"));
        if (isDbHealthy) {
            return ResponseEntity.ok(HealthResponse.up(components));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(HealthResponse.down(components));
        }
    }
}
