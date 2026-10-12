package com.smarttracker.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /api/health should return 200 OK and UP status with database connectivity")
    void shouldReturnHealthyStatusFromLivePostgres() {
        String url = "http://localhost:" + port + "/api/health";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("timestamp")).isNotNull();

        Map<?, ?> components = (Map<?, ?>) response.getBody().get("components");
        assertThat(components).isNotNull();

        Map<?, ?> app = (Map<?, ?>) components.get("application");
        assertThat(app).isNotNull();
        assertThat(app.get("status")).isEqualTo("UP");
        assertThat(app.get("name")).isEqualTo("smart-expense-tracker");

        Map<?, ?> db = (Map<?, ?>) components.get("database");
        assertThat(db).isNotNull();
        assertThat(db.get("status")).isEqualTo("UP");
        assertThat(db.get("database")).isEqualTo("PostgreSQL");
    }
}
