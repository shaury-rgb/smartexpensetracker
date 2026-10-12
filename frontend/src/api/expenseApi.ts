import { apiClient } from './client';
import { Expense, ExpenseRequest, PageResponse } from '../types';

export interface ExpenseQueryParams {
  category?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export const expenseApi = {
  async getExpenses(params: ExpenseQueryParams = {}): Promise<PageResponse<Expense>> {
    const response = await apiClient.get<PageResponse<Expense>>('/api/expenses', {
      params,
    });
    return response.data;
  },

  async getExpenseById(id: number): Promise<Expense> {
    const response = await apiClient.get<Expense>(`/api/expenses/${id}`);
    return response.data;
  },

  async createExpense(data: ExpenseRequest): Promise<Expense> {
    const response = await apiClient.post<Expense>('/api/expenses', data);
    return response.data;
  },

  async updateExpense(id: number, data: ExpenseRequest): Promise<Expense> {
    const response = await apiClient.put<Expense>(`/api/expenses/${id}`, data);
    return response.data;
  },

  async deleteExpense(id: number): Promise<void> {
    await apiClient.delete(`/api/expenses/${id}`);
  },
};
