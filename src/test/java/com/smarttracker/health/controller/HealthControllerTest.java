package com.smarttracker.health.controller;

import com.smarttracker.health.service.DatabaseHealthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseHealthService databaseHealthService;

    @Test
    @DisplayName("GET /api/health should return 200 OK when database is UP")
    void shouldReturnOkWhenDatabaseIsHealthy() throws Exception {
        Map<String, Object> dbHealth = new LinkedHashMap<>();
        dbHealth.put("status", "UP");
        dbHealth.put("database", "PostgreSQL");
        dbHealth.put("version", "17.0");
        dbHealth.put("appHealthRecordsCount", 1L);

        when(databaseHealthService.checkHealth()).thenReturn(dbHealth);

        mockMvc.perform(get("/api/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.components.application.status").value("UP"))
                .andExpect(jsonPath("$.components.application.name").value("smart-expense-tracker"))
                .andExpect(jsonPath("$.components.database.status").value("UP"))
                .andExpect(jsonPath("$.components.database.database").value("PostgreSQL"));
    }

    @Test
    @DisplayName("GET /api/health should return 503 Service Unavailable when database is DOWN")
    void shouldReturnServiceUnavailableWhenDatabaseIsDown() throws Exception {
        Map<String, Object> dbHealth = new LinkedHashMap<>();
        dbHealth.put("status", "DOWN");
        dbHealth.put("error", "Connection refused");

        when(databaseHealthService.checkHealth()).thenReturn(dbHealth);

        mockMvc.perform(get("/api/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.components.application.status").value("UP"))
                .andExpect(jsonPath("$.components.database.status").value("DOWN"))
                .andExpect(jsonPath("$.components.database.error").value("Connection refused"));
    }
}
