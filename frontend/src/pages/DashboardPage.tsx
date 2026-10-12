import React, { useState, useEffect, useCallback } from 'react';
import {
  IndianRupee,
  ShoppingBag,
  TrendingDown,
  TrendingUp,
  Percent,
  Plus,
  RefreshCw,
  Calendar,
  AlertTriangle,
  ArrowRight,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { analyticsApi } from '../api/analyticsApi';
import { categoryApi } from '../api/categoryApi';
import {
  DashboardSummary,
  CategoryBreakdown,
  MonthlyTrend,
  Expense,
  Category,
} from '../types';
import { StatCard } from '../components/common/StatCard';
import { CardSkeleton } from '../components/common/LoadingSkeleton';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { EmptyState } from '../components/common/EmptyState';
import { CategoryDoughnutChart } from '../components/charts/CategoryDoughnutChart';
import { MonthlyTrendBarChart } from '../components/charts/MonthlyTrendBarChart';
import { ExpenseModal } from '../components/expenses/ExpenseModal';
import { formatCurrency, formatDate } from '../utils/formatters';

export const DashboardPage: React.FC = () => {
  const currentDate = new Date();
  const [selectedMonth, setSelectedMonth] = useState<number>(currentDate.getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState<number>(currentDate.getFullYear());

  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [categoriesBreakdown, setCategoriesBreakdown] = useState<CategoryBreakdown[]>([]);
  const [monthlyTrends, setMonthlyTrends] = useState<MonthlyTrend[]>([]);
  const [recentExpenses, setRecentExpenses] = useState<Expense[]>([]);
  const [topExpenses, setTopExpenses] = useState<Expense[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState(false);

  const fetchDashboardData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [sumRes, catRes, trendsRes, recentRes, topRes, allCatsRes] = await Promise.all([
        analyticsApi.getSummary(selectedMonth, selectedYear),
        analyticsApi.getCategoryBreakdown(selectedMonth, selectedYear),
        analyticsApi.getMonthlyTrend(6, selectedMonth, selectedYear),
        analyticsApi.getRecentExpenses(5),
        analyticsApi.getTopExpenses(selectedMonth, selectedYear, 5),
        categoryApi.getCategories(),
      ]);

      setSummary(sumRes);
      setCategoriesBreakdown(catRes);
      setMonthlyTrends(trendsRes);
      setRecentExpenses(recentRes);
      setTopExpenses(topRes);
      setCategories(allCatsRes);
    } catch (err: any) {
      setError(err?.message || 'Failed to load dashboard data. Please check your connection.');
    } finally {
      setLoading(false);
    }
  }, [selectedMonth, selectedYear]);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  // Compute budget percentage
  const budgetUtilization = summary?.budgetAmount && summary.budgetAmount > 0
    ? (Number(summary.totalSpending) / Number(summary.budgetAmount)) * 100
    : null;
  const isOverBudget = budgetUtilization !== null && budgetUtilization > 100;

  return (
    <div className="space-y-6">
      {/* Top Header: Title, Month/Year selector, Quick Add */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Financial Dashboard</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Overview of spending, budget progress, and insights
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Month Selector */}
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
            onClick={() => fetchDashboardData()}
            title="Refresh statistics"
            className="p-2 text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-xs transition-colors cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <button
            onClick={() => setIsExpenseModalOpen(true)}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 shadow-md shadow-indigo-600/20 transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Add Expense</span>
          </button>
        </div>
      </div>

      {/* Error notification if any */}
      {error && <ErrorAlert message={error} onRetry={fetchDashboardData} />}

      {/* Over budget banner */}
      {isOverBudget && (
        <div className="flex items-center gap-3 p-4 rounded-xl border border-amber-300 dark:border-amber-900 bg-amber-50 dark:bg-amber-950/40 text-amber-800 dark:text-amber-300">
          <AlertTriangle className="w-5 h-5 flex-shrink-0 text-amber-500" />
          <div className="text-sm">
            <span className="font-semibold">Budget Limit Exceeded:</span> You have utilized{' '}
            <span className="font-bold">{budgetUtilization?.toFixed(1)}%</span> of your monthly budget.
          </div>
        </div>
      )}

      {/* Primary KPI Summary Cards */}
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
              title="Total Spending"
              value={formatCurrency(summary?.totalSpending ?? 0)}
              subtitle={`${summary?.expenseCount ?? 0} transactions logged`}
              icon={IndianRupee}
              color="blue"
            />
            <StatCard
              title="Average Expense"
              value={formatCurrency(summary?.averageExpenseAmount ?? 0)}
              subtitle={`Highest: ${formatCurrency(summary?.highestExpenseAmount ?? 0)}`}
              icon={ShoppingBag}
              color="purple"
            />
            <StatCard
              title="Month-over-Month"
              value={
                summary?.percentageChange !== null &&
                summary?.percentageChange !== undefined
                  ? `${summary.percentageChange >= 0 ? '+' : ''}${Number(summary.percentageChange).toFixed(1)}%`
                  : 'N/A'
              }
              subtitle={
                summary?.absoluteChange !== null &&
                summary?.absoluteChange !== undefined
                  ? `${formatCurrency(Math.abs(summary.absoluteChange))} vs last month`
                  : 'No previous month data'
              }
              icon={
                summary?.percentageChange &&
                summary.percentageChange > 0
                  ? TrendingUp
                  : TrendingDown
              }
              trend={
                summary?.percentageChange !== null &&
                summary?.percentageChange !== undefined
                  ? {
                      value: Number(summary.percentageChange),
                      isPositiveGood: false,
                    }
                  : undefined
              }
              color="amber"
            />
            <StatCard
              title="Budget Utilization"
              value={
                budgetUtilization !== null
                  ? `${budgetUtilization.toFixed(1)}%`
                  : 'No Budget'
              }
              subtitle={
                summary?.remainingBudget !== null && summary?.remainingBudget !== undefined
                  ? `${formatCurrency(summary.remainingBudget)} remaining`
                  : 'Configure in Budgets tab'
              }
              icon={Percent}
              color={isOverBudget ? 'rose' : 'emerald'}
            />
          </>
        )}
      </div>

      {/* Visual Analytics Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Breakdown Doughnut */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                Spending by Category
              </h3>
              <p className="text-xs text-slate-400">Distribution for selected calendar month</p>
            </div>
            <Link
              to="/analytics"
              className="text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline flex items-center gap-1"
            >
              Details <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
          {loading ? (
            <div className="h-64 flex items-center justify-center">
              <span className="w-6 h-6 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <CategoryDoughnutChart breakdown={categoriesBreakdown} />
          )}
        </div>

        {/* 6-Month Spending Trend Bar */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                Recent Monthly Trend
              </h3>
              <p className="text-xs text-slate-400">Last 6 months aggregate spending</p>
            </div>
            <Link
              to="/analytics"
              className="text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline flex items-center gap-1"
            >
              View Analytics <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
          {loading ? (
            <div className="h-64 flex items-center justify-center">
              <span className="w-6 h-6 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <MonthlyTrendBarChart trends={monthlyTrends} />
          )}
        </div>
      </div>

      {/* Recent & Top Expenses Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Expenses List (2 columns) */}
        <div className="lg:col-span-2 p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                Recent Transactions
              </h3>
              <p className="text-xs text-slate-400">Latest expenses across all categories</p>
            </div>
            <Link
              to="/expenses"
              className="text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline flex items-center gap-1"
            >
              View All <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {recentExpenses.length === 0 ? (
            <EmptyState
              title="No expenses found"
              description="Record your first expense to begin tracking your spending."
              actionText="Add Expense"
              onAction={() => setIsExpenseModalOpen(true)}
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-slate-100 dark:border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                    <th className="py-2.5 px-3">Title</th>
                    <th className="py-2.5 px-3">Category</th>
                    <th className="py-2.5 px-3">Date</th>
                    <th className="py-2.5 px-3 text-right">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60">
                  {recentExpenses.map((exp) => (
                    <tr key={exp.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/40">
                      <td className="py-3 px-3 font-medium text-slate-800 dark:text-slate-200">
                        {exp.title}
                        {exp.paymentMethod && (
                          <span className="block text-[11px] text-slate-400 font-normal">
                            {exp.paymentMethod}
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-3">
                        <span className="inline-flex px-2 py-0.5 rounded-md text-xs font-medium bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                          {exp.category}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-xs text-slate-500 dark:text-slate-400">
                        {formatDate(exp.expenseDate)}
                      </td>
                      <td className="py-3 px-3 text-right font-semibold text-slate-900 dark:text-slate-100">
                        {formatCurrency(exp.amount)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Top Expenses in Month (1 column) */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                  Top Expenses
                </h3>
                <p className="text-xs text-slate-400">Largest purchases this month</p>
              </div>
            </div>

            {topExpenses.length === 0 ? (
              <p className="text-xs text-slate-400 py-6 text-center italic">
                No high-value expenses recorded this month.
              </p>
            ) : (
              <div className="space-y-3">
                {topExpenses.map((exp, idx) => (
                  <div
                    key={exp.id}
                    className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-6 h-6 rounded-full bg-indigo-100 dark:bg-indigo-950 flex items-center justify-center text-xs font-bold text-indigo-600 dark:text-indigo-400 flex-shrink-0">
                        {idx + 1}
                      </div>
                      <div className="min-w-0">
                        <p className="text-xs font-semibold text-slate-800 dark:text-slate-200 truncate">
                          {exp.title}
                        </p>
                        <p className="text-[11px] text-slate-400">{exp.category}</p>
                      </div>
                    </div>
                    <span className="text-xs font-bold text-slate-900 dark:text-slate-100 flex-shrink-0">
                      {formatCurrency(exp.amount)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 text-center">
            <button
              onClick={() => setIsExpenseModalOpen(true)}
              className="text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline cursor-pointer"
            >
              + Record another expense
            </button>
          </div>
        </div>
      </div>

      {/* Add Expense Modal */}
      <ExpenseModal
        isOpen={isExpenseModalOpen}
        onClose={() => setIsExpenseModalOpen(false)}
        onSuccess={() => fetchDashboardData()}
        categories={categories}
      />
    </div>
  );
};
