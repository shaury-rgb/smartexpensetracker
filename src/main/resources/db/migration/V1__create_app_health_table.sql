-- Initial migration: create app_health check table
CREATE TABLE IF NOT EXISTS app_health (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

INSERT INTO app_health (status) VALUES ('HEALTHY');
