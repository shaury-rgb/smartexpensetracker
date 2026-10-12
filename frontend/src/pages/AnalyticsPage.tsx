import React, { useState, useEffect, useCallback } from 'react';
import {
  Calendar,
  RefreshCw,
  TrendingUp,
  TrendingDown,
  IndianRupee,
  Receipt,
  PieChart as PieIcon,
  BarChart3,
  Activity,
} from 'lucide-react';
import { analyticsApi } from '../api/analyticsApi';
import {
  DashboardSummary,
  CategoryBreakdown,
  MonthlyTrend,
  DailyTrend,
  Expense,
  BudgetStatusAnalytics,
  CategoryBudgetStatus,
} from '../types';
import { formatCurrency, formatDate } from '../utils/formatters';
import { StatCard } from '../components/common/StatCard';
import { CardSkeleton } from '../components/common/LoadingSkeleton';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { CategoryDoughnutChart } from '../components/charts/CategoryDoughnutChart';
import { MonthlyTrendBarChart } from '../components/charts/MonthlyTrendBarChart';
import { DailyTrendLineChart } from '../components/charts/DailyTrendLineChart';

export const AnalyticsPage: React.FC = () => {
  const currentDate = new Date();
  const [selectedMonth, setSelectedMonth] = useState<number>(currentDate.getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState<number>(currentDate.getFullYear());

  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [categoryBreakdown, setCategoryBreakdown] = useState<CategoryBreakdown[]>([]);
  const [monthlyTrends, setMonthlyTrends] = useState<MonthlyTrend[]>([]);
  const [dailyTrends, setDailyTrends] = useState<DailyTrend[]>([]);
  const [topExpenses, setTopExpenses] = useState<Expense[]>([]);
  const [budgetStatus, setBudgetStatus] = useState<BudgetStatusAnalytics | null>(null);

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAnalytics = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [sum, cat, monthly, daily, top, budget] = await Promise.all([
        analyticsApi.getSummary(selectedMonth, selectedYear),
        analyticsApi.getCategoryBreakdown(selectedMonth, selectedYear),
        analyticsApi.getMonthlyTrend(6, selectedMonth, selectedYear),
        analyticsApi.getDailyTrend(selectedMonth, selectedYear),
        analyticsApi.getTopExpenses(selectedMonth, selectedYear, 5),
        analyticsApi.getBudgetStatus(selectedMonth, selectedYear),
      ]);

      setSummary(sum);
      setCategoryBreakdown(cat);
      setMonthlyTrends(monthly);
      setDailyTrends(daily);
      setTopExpenses(top);
      setBudgetStatus(budget);
    } catch (err: any) {
      setError(err?.message || 'Failed to load analytics data.');
    } finally {
      setLoading(false);
    }
  }, [selectedMonth, selectedYear]);

  useEffect(() => {
    fetchAnalytics();
  }, [fetchAnalytics]);

  return (
    <div className="space-y-6">
      {/* Header & Filters */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Financial Analytics</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Deep dive into spending patterns, velocity, category weights, and comparisons
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl px-3 py-1.5 shadow-xs">
            <Calendar className="w-4 h-4 text-slate-400" />
            <select
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(Number(e.target.value))}
              className="text-xs sm:text-sm font-medium bg-transparent text-slate-800 dark:text-slate-200 focus:outline-none cursor-pointer"
            >
              {[
                'January', 'February', 'March', 'April', 'May', 'June',
                'July', 'August', 'September', 'October', 'November', 'December'
              ].map((m, idx) => (
                <option key={idx + 1} value={idx + 1}>
                  {m}
                </option>
              ))}
            </select>
            <select
              value={selectedYear}
              onChange={(e) => setSelectedYear(Number(e.target.value))}
              className="text-xs sm:text-sm font-medium bg-transparent text-slate-800 dark:text-slate-200 focus:outline-none cursor-pointer"
            >
              {[2024, 2025, 2026, 2027].map((yr) => (
                <option key={yr} value={yr}>
                  {yr}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={() => fetchAnalytics()}
            title="Refresh"
            className="p-2 text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-xs transition-colors cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {error && <ErrorAlert message={error} onRetry={fetchAnalytics} onDismiss={() => setError(null)} />}

      {/* KPI Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {loading ? (
          <>
            <CardSkeleton />
            <CardSkeleton />
            <CardSkeleton />
            <CardSkeleton />
          </>
        ) : (
          <>
            <StatCard
              title="Total Spent"
              value={formatCurrency(summary?.totalSpending ?? 0)}
              subtitle={`${summary?.expenseCount ?? 0} transactions in month`}
              icon={IndianRupee}
              color="blue"
            />
            <StatCard
              title="Avg Transaction"
              value={formatCurrency(summary?.averageExpenseAmount ?? 0)}
              subtitle={`Peak: ${formatCurrency(summary?.highestExpenseAmount ?? 0)}`}
              icon={Receipt}
              color="purple"
            />
            <StatCard
              title="MoM Velocity"
              value={
                summary?.percentageChange !== null && summary?.percentageChange !== undefined
                  ? `${summary.percentageChange >= 0 ? '+' : ''}${Number(summary.percentageChange).toFixed(1)}%`
                  : 'N/A'
              }
              subtitle={
                summary?.absoluteChange !== null && summary?.absoluteChange !== undefined
                  ? `${summary.absoluteChange >= 0 ? '+' : '-'}${formatCurrency(Math.abs(summary.absoluteChange))} vs prev month`
                  : 'No prior data'
              }
              icon={
                summary?.percentageChange && summary.percentageChange > 0
                  ? TrendingUp
                  : TrendingDown
              }
              trend={
                summary?.percentageChange !== null && summary?.percentageChange !== undefined
                  ? {
                      value: Number(summary.percentageChange),
                      isPositiveGood: false,
                    }
                  : undefined
              }
              color="amber"
            />
            <StatCard
              title="Budget Ceiling"
              value={
                budgetStatus?.overallBudget.configured
                  ? formatCurrency(budgetStatus.overallBudget.budgetAmount ?? 0)
                  : 'Not Set'
              }
              subtitle={
                budgetStatus?.overallBudget.configured
                  ? `${budgetStatus.overallBudget.percentageUsed?.toFixed(1)}% utilized`
                  : 'Uncapped month'
              }
              icon={Activity}
              color="emerald"
            />
          </>
        )}
      </div>

      {/* Daily Spending Trend Chart */}
      <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
              Daily Spending Rhythm
            </h3>
            <p className="text-xs text-slate-400">Day-by-day expenditure throughout the calendar month</p>
          </div>
        </div>
        {loading ? (
          <div className="h-64 flex items-center justify-center">
            <RefreshCw className="w-6 h-6 text-indigo-600 animate-spin" />
          </div>
        ) : (
          <DailyTrendLineChart dailyData={dailyTrends} />
        )}
      </div>

      {/* Two columns: Category Breakdown & Monthly Trend */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Breakdown */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                Category Spending Allocation
              </h3>
              <p className="text-xs text-slate-400">Percentage weight per category</p>
            </div>
            <PieIcon className="w-4 h-4 text-slate-400" />
          </div>
          {loading ? (
            <div className="h-64 flex items-center justify-center">
              <RefreshCw className="w-6 h-6 text-indigo-600 animate-spin" />
            </div>
          ) : (
            <CategoryDoughnutChart breakdown={categoryBreakdown} />
          )}
        </div>

        {/* 6-Month Macro Trend */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                Monthly Spending Trajectory
              </h3>
              <p className="text-xs text-slate-400">Multi-month comparative volume</p>
            </div>
            <BarChart3 className="w-4 h-4 text-slate-400" />
          </div>
          {loading ? (
            <div className="h-64 flex items-center justify-center">
              <RefreshCw className="w-6 h-6 text-indigo-600 animate-spin" />
            </div>
          ) : (
            <MonthlyTrendBarChart trends={monthlyTrends} />
          )}
        </div>
      </div>

      {/* Highest Value Expenses & Category Budgets Table */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top 5 High-Value Expenses */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
              Highest-Value Expenses
            </h3>
            <p className="text-xs text-slate-400">Significant individual transactions for this period</p>
          </div>

          {topExpenses.length === 0 ? (
            <p className="text-xs text-slate-400 py-6 text-center italic">
              No expenses recorded in this period.
            </p>
          ) : (
            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {topExpenses.map((exp, idx) => (
                <div key={exp.id} className="py-3 flex items-center justify-between">
                  <div className="flex items-center gap-3 min-w-0">
                    <span className="w-6 h-6 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-xs font-semibold text-slate-600 dark:text-slate-300 flex-shrink-0">
                      {idx + 1}
                    </span>
                    <div className="min-w-0">
                      <p className="text-xs font-semibold text-slate-800 dark:text-slate-200 truncate">
                        {exp.title}
                      </p>
                      <p className="text-[11px] text-slate-400">
                        {exp.category} • {formatDate(exp.expenseDate)}
                      </p>
                    </div>
                  </div>
                  <span className="text-xs font-bold text-slate-900 dark:text-slate-100">
                    {formatCurrency(exp.amount)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Category Budget Status breakdown */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
              Category Budget Adherence
            </h3>
            <p className="text-xs text-slate-400">Live compliance against target limits</p>
          </div>

          {!budgetStatus?.categoryBudgets || budgetStatus.categoryBudgets.length === 0 ? (
            <p className="text-xs text-slate-400 py-6 text-center italic">
              No category budgets set for this month.
            </p>
          ) : (
            <div className="space-y-3">
              {budgetStatus.categoryBudgets.map((cb: CategoryBudgetStatus) => (
                <div key={cb.categoryId} className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/40 space-y-1.5">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-slate-800 dark:text-slate-200">
                      {cb.categoryName}
                    </span>
                    <span className={cb.exceeded ? 'text-rose-600 font-bold' : 'text-slate-500'}>
                      {formatCurrency(cb.actualSpending)} / {formatCurrency(cb.budgetAmount)} ({cb.percentageUsed.toFixed(1)}%)
                    </span>
                  </div>
                  <div className="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-1.5 overflow-hidden">
                    <div
                      className={`h-1.5 rounded-full ${cb.exceeded ? 'bg-rose-500' : 'bg-indigo-600'}`}
                      style={{ width: `${Math.min(cb.percentageUsed, 100)}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
