-- V6: Add composite index for efficient user-scoped date queries
CREATE INDEX IF NOT EXISTS idx_expenses_user_expense_date ON expenses (user_id, expense_date);
