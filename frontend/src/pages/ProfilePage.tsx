import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { formatDate } from '../utils/formatters';
import { User, ShieldCheck, Mail, Calendar, Key, Moon, Sun, LogOut } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">User Profile</h2>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Account details, authentication credentials, and security settings
        </p>
      </div>

      {/* User Information Card */}
      <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
        <div className="flex items-center gap-4 pb-6 border-b border-slate-100 dark:border-slate-800">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-indigo-600 to-violet-500 text-white flex items-center justify-center text-2xl font-bold shadow-md shadow-indigo-500/20">
            {user?.name ? user.name.slice(0, 2).toUpperCase() : <User className="w-8 h-8" />}
          </div>
          <div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100">{user?.name}</h3>
            <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 mt-1 rounded-full text-xs font-semibold bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
              <ShieldCheck className="w-3.5 h-3.5" />
              Authenticated User
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center gap-3">
            <Mail className="w-5 h-5 text-slate-400" />
            <div>
              <p className="text-xs text-slate-400 font-medium">Email Address</p>
              <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                {user?.email || 'N/A'}
              </p>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center gap-3">
            <Calendar className="w-5 h-5 text-slate-400" />
            <div>
              <p className="text-xs text-slate-400 font-medium">Account Created</p>
              <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                {user?.createdAt ? formatDate(user.createdAt) : 'N/A'}
              </p>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center gap-3">
            <Key className="w-5 h-5 text-slate-400" />
            <div>
              <p className="text-xs text-slate-400 font-medium">User Identifier</p>
              <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                ID #{user?.id ?? 'N/A'}
              </p>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center justify-between">
            <div className="flex items-center gap-3">
              {theme === 'dark' ? (
                <Moon className="w-5 h-5 text-amber-400" />
              ) : (
                <Sun className="w-5 h-5 text-slate-500" />
              )}
              <div>
                <p className="text-xs text-slate-400 font-medium">Appearance</p>
                <p className="text-sm font-semibold text-slate-800 dark:text-slate-200 capitalize">
                  {theme} Mode
                </p>
              </div>
            </div>
            <button
              onClick={toggleTheme}
              className="text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline cursor-pointer"
            >
              Toggle
            </button>
          </div>
        </div>

        {/* Security architecture notes */}
        <div className="p-5 rounded-xl border border-indigo-100 dark:border-indigo-950/80 bg-indigo-50/50 dark:bg-indigo-950/20 text-xs text-slate-600 dark:text-slate-300 space-y-2">
          <div className="flex items-center gap-2 font-bold text-indigo-700 dark:text-indigo-300">
            <ShieldCheck className="w-4 h-4" />
            <span>Security Architecture & Token Handling</span>
          </div>
          <p>
            The application secures communication using JWT Bearer tokens signed with HMAC-SHA256. All private API endpoints are strictly authenticated and user-scoped in the Spring Boot backend via Spring Security filters. Client queries enforce strict tenant isolation, guaranteeing users cannot read, edit, or delete another user's financial records.
          </p>
          <p className="text-[11px] text-slate-400">
            <strong>Security Notice:</strong> Bearer tokens stored in browser local storage are susceptible to client-side script inspection if XSS vulnerabilities exist. For enterprise production deployments, HttpOnly cookies with CSRF tokens and SameSite=Strict protection provide an additional defense-in-depth layer.
          </p>
        </div>

        <div className="pt-4 border-t border-slate-100 dark:border-slate-800 flex justify-end">
          <button
            onClick={logout}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold text-white bg-rose-600 hover:bg-rose-700 rounded-xl transition-colors cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out of Account</span>
          </button>
        </div>
      </div>
    </div>
  );
};
