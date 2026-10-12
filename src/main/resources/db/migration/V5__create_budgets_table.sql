-- Phase 3: Create budgets table with constraints and unique scoping
CREATE TABLE IF NOT EXISTS budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_budget_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_budget_month_valid CHECK (month BETWEEN 1 AND 12),
    CONSTRAINT chk_budget_year_valid CHECK (year >= 2000)
);

-- Unique overall budget per user, month, and year
CREATE UNIQUE INDEX IF NOT EXISTS idx_budgets_user_overall ON budgets (user_id, month, year) WHERE category_id IS NULL;

-- Unique category budget per user, month, year, and category
CREATE UNIQUE INDEX IF NOT EXISTS idx_budgets_user_category ON budgets (user_id, month, year, category_id) WHERE category_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_budgets_user_date ON budgets (user_id, year, month);
