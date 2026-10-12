/**
 * Token and User Session Storage
 * 
 * Security Note:
 * The backend authenticates requests via RFC 6750 Bearer Tokens (JWT).
 * Browser storage (localStorage/sessionStorage) carries XSS exposure risks.
 * In production enterprise environments, token transmission via HttpOnly,
 * SameSite cookies is recommended when architectural constraints allow.
 * Here we centralize token access with safety checks, type validation,
 * and automatic synchronization with HTTP request interceptors.
 */

const TOKEN_KEY = 'smart_expense_tracker_token';
const USER_KEY = 'smart_expense_tracker_user';

export const storage = {
  getToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },

  setToken(token: string): void {
    try {
      localStorage.setItem(TOKEN_KEY, token);
    } catch (err) {
      console.warn('Failed to persist token to storage:', err);
    }
  },

  clearToken(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch {}
  },

  getUser<T>(): T | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  },

  setUser<T>(user: T): void {
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } catch (err) {
      console.warn('Failed to persist user to storage:', err);
    }
  },

  clearUser(): void {
    try {
      localStorage.removeItem(USER_KEY);
    } catch {}
  },

  clearAll(): void {
    this.clearToken();
    this.clearUser();
  }
};
