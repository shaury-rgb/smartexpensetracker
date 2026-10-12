import { apiClient } from './client';
import { Category } from '../types';

export const categoryApi = {
  async getCategories(): Promise<Category[]> {
    const response = await apiClient.get<Category[]>('/api/categories');
    return response.data;
  },

  async createCategory(name: string): Promise<Category> {
    const response = await apiClient.post<Category>('/api/categories', { name });
    return response.data;
  },

  async updateCategory(id: number, name: string): Promise<Category> {
    const response = await apiClient.put<Category>(`/api/categories/${id}`, { name });
    return response.data;
  },

  async deleteCategory(id: number): Promise<void> {
    await apiClient.delete(`/api/categories/${id}`);
  },
};
