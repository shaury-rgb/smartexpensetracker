import { apiClient } from './client';
import {
  BudgetStatusAnalytics,
  CategoryBreakdown,
  DailyTrend,
  DashboardSummary,
  Expense,
  MonthlyTrend,
} from '../types';

export const analyticsApi = {
  async getSummary(month: number, year: number): Promise<DashboardSummary> {
    const response = await apiClient.get<DashboardSummary>('/api/analytics/summary', {
      params: { month, year },
    });
    return response.data;
  },

  async getCategoryBreakdown(month: number, year: number): Promise<CategoryBreakdown[]> {
    const response = await apiClient.get<CategoryBreakdown[]>('/api/analytics/category-breakdown', {
      params: { month, year },
    });
    return response.data;
  },

  async getMonthlyTrend(months: number = 6, endMonth?: number, endYear?: number): Promise<MonthlyTrend[]> {
    const params: Record<string, number> = { months };
    if (endMonth) params.endMonth = endMonth;
    if (endYear) params.endYear = endYear;

    const response = await apiClient.get<MonthlyTrend[]>('/api/analytics/monthly-trend', {
      params,
    });
    return response.data;
  },

  async getDailyTrend(month: number, year: number): Promise<DailyTrend[]> {
    const response = await apiClient.get<DailyTrend[]>('/api/analytics/daily-trend', {
      params: { month, year },
    });
    return response.data;
  },

  async getRecentExpenses(limit: number = 5): Promise<Expense[]> {
    const response = await apiClient.get<Expense[]>('/api/analytics/recent-expenses', {
      params: { limit },
    });
    return response.data;
  },

  async getTopExpenses(month: number, year: number, limit: number = 5): Promise<Expense[]> {
    const response = await apiClient.get<Expense[]>('/api/analytics/top-expenses', {
      params: { month, year, limit },
    });
    return response.data;
  },

  async getBudgetStatus(month: number, year: number): Promise<BudgetStatusAnalytics> {
    const response = await apiClient.get<BudgetStatusAnalytics>('/api/analytics/budget-status', {
      params: { month, year },
    });
    return response.data;
  },
};
