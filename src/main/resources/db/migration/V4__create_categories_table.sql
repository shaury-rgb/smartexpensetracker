-- Phase 3: Create categories table with system categories and user-owned custom categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_system BOOLEAN DEFAULT FALSE NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Unique index for system categories (case-insensitive)
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_system_name ON categories (LOWER(name)) WHERE user_id IS NULL;

-- Unique index for user custom categories (case-insensitive per user)
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_user_name ON categories (user_id, LOWER(name)) WHERE user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories (user_id);

-- Pre-seed default system categories
INSERT INTO categories (name, is_system, user_id) VALUES
    ('Food', TRUE, NULL),
    ('Transport', TRUE, NULL),
    ('Utilities', TRUE, NULL),
    ('Entertainment', TRUE, NULL),
    ('Housing', TRUE, NULL),
    ('Healthcare', TRUE, NULL),
    ('Shopping', TRUE, NULL),
    ('Personal', TRUE, NULL),
    ('Other', TRUE, NULL)
ON CONFLICT DO NOTHING;
