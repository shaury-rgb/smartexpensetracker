-- Phase 3: Create users table and establish user ownership on expenses
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- Add user_id column referencing users(id)
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

-- If there are no orphan rows without user_id, set NOT NULL constraint
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM expenses WHERE user_id IS NULL) THEN
        ALTER TABLE expenses ALTER COLUMN user_id SET NOT NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_expenses_user_id ON expenses (user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_user_date ON expenses (user_id, expense_date);
