import { apiClient } from './client';
import { AuthResponse, User } from '../types';

export const authApi = {
  async login(credentials: { email: string; password: string }): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/api/auth/login', credentials);
    return response.data;
  },

  async register(data: { name: string; email: string; password: string }): Promise<User> {
    const response = await apiClient.post<User>('/api/auth/register', data);
    return response.data;
  },

  async getMe(): Promise<User> {
    const response = await apiClient.get<User>('/api/auth/me');
    return response.data;
  },
};
