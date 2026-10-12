export interface User {
  id: number;
  name: string;
  email: string;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: User;
}

export interface Expense {
  id: number;
  title: string;
  amount: number;
  category: string;
  description?: string;
  expenseDate: string;
  paymentMethod?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ExpenseRequest {
  title: string;
  amount: number;
  category: string;
  description?: string;
  expenseDate: string;
  paymentMethod?: string;
}

export interface PageResponse<T> {
  content: T[];
  page?: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
  totalElements?: number;
  totalPages?: number;
  size?: number;
  number?: number;
}

export interface Category {
  id: number;
  name: string;
  isSystem: boolean;
  createdAt: string;
}

export interface Budget {
  id: number;
  categoryId?: number | null;
  categoryName?: string | null;
  amount: number;
  month: number;
  year: number;
  actualSpending: number;
  remainingAmount: number;
  percentageUsed: number;
  createdAt: string;
  updatedAt: string;
}

export interface BudgetRequest {
  categoryId?: number | null;
  amount: number;
  month: number;
  year: number;
}

export interface DashboardSummary {
  month: number;
  year: number;
  totalSpending: number;
  expenseCount: number;
  averageExpenseAmount: number;
  highestExpenseAmount?: number | null;
  lowestExpenseAmount?: number | null;
  previousMonthTotalSpending: number;
  absoluteChange: number;
  percentageChange: number;
  budgetAmount?: number | null;
  remainingBudget?: number | null;
}

export interface CategoryBreakdown {
  categoryName: string;
  totalSpending: number;
  transactionCount: number;
  percentage: number;
}

export interface MonthlyTrend {
  year: number;
  month: number;
  monthLabel: string;
  totalSpending: number;
  transactionCount: number;
}

export interface DailyTrend {
  date: string;
  day: number;
  totalSpending: number;
  transactionCount: number;
}

export interface OverallBudgetStatus {
  configured: boolean;
  budgetAmount?: number | null;
  actualSpending: number;
  remainingAmount?: number | null;
  percentageUsed?: number | null;
  exceeded: boolean;
}

export interface CategoryBudgetStatus {
  categoryId: number;
  categoryName: string;
  budgetAmount: number;
  actualSpending: number;
  remainingAmount: number;
  percentageUsed: number;
  exceeded: boolean;
}

export interface BudgetStatusAnalytics {
  month: number;
  year: number;
  overallBudget: OverallBudgetStatus;
  categoryBudgets: CategoryBudgetStatus[];
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
  fieldErrors?: Record<string, string>;
}
