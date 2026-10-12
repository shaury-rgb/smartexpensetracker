import axios, { AxiosError } from 'axios';
import { storage } from '../utils/storage';
import { ApiError } from '../types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  timeout: 15000,
});

apiClient.interceptors.request.use(
  (config) => {
    const token = storage.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      // Don't auto-redirect if the 401 is on login or register endpoints
      const url = error.config?.url || '';
      if (!url.includes('/api/auth/login') && !url.includes('/api/auth/register')) {
        storage.clearAll();
        window.dispatchEvent(new CustomEvent('auth:unauthorized'));
      }
    }
    return Promise.reject(error);
  }
);

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiError | undefined;
    if (apiError) {
      if (apiError.fieldErrors && Object.keys(apiError.fieldErrors).length > 0) {
        return Object.values(apiError.fieldErrors).join(', ');
      }
      if (apiError.message) {
        return apiError.message;
      }
    }
    if (error.response?.status === 401) {
      return 'Invalid email or password. Please check your credentials.';
    }
    if (error.response?.status === 403) {
      return 'You do not have permission to perform this action.';
    }
    if (error.response?.status === 404) {
      return 'Requested resource was not found.';
    }
    if (error.response?.status === 409) {
      return 'Conflict: Resource already exists or cannot be modified.';
    }
    if (error.code === 'ECONNABORTED') {
      return 'Request timed out. Please check your server connection.';
    }
    if (error.message) {
      return error.message;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'An unexpected error occurred. Please try again.';
}
