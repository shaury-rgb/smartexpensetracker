import { apiClient } from './client';
import { Budget, BudgetRequest } from '../types';

export const budgetApi = {
  async getBudgets(month: number, year: number): Promise<Budget[]> {
    const response = await apiClient.get<Budget[]>('/api/budgets', {
      params: { month, year },
    });
    return response.data;
  },

  async createBudget(data: BudgetRequest): Promise<Budget> {
    const response = await apiClient.post<Budget>('/api/budgets', data);
    return response.data;
  },

  async updateBudget(id: number, amount: number): Promise<Budget> {
    const response = await apiClient.put<Budget>(`/api/budgets/${id}`, { amount });
    return response.data;
  },

  async deleteBudget(id: number): Promise<void> {
    await apiClient.delete(`/api/budgets/${id}`);
  },
};
