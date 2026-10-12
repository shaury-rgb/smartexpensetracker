package com.smarttracker.health.service;

import com.smarttracker.health.repository.AppHealthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DatabaseHealthService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthService.class);

    private final DataSource dataSource;
    private final AppHealthRepository appHealthRepository;

    public DatabaseHealthService(DataSource dataSource, AppHealthRepository appHealthRepository) {
        this.dataSource = dataSource;
        this.appHealthRepository = appHealthRepository;
    }

    public Map<String, Object> checkHealth() {
        Map<String, Object> details = new LinkedHashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);
            if (!isValid) {
                details.put("status", "DOWN");
                details.put("error", "Database connection validation failed");
                return details;
            }

            DatabaseMetaData metaData = connection.getMetaData();
            details.put("status", "UP");
            details.put("database", metaData.getDatabaseProductName());
            details.put("version", metaData.getDatabaseProductVersion());

            long healthRecordsCount = appHealthRepository.count();
            details.put("appHealthRecordsCount", healthRecordsCount);
            details.put("message", "PostgreSQL connection and Flyway migration verified successfully");
        } catch (Exception e) {
            log.error("Database connectivity check failed: {}", e.getMessage(), e);
            details.put("status", "DOWN");
            details.put("error", e.getMessage());
        }

        return details;
    }
}
