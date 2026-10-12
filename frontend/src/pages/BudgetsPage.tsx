import React, { useState, useEffect, useCallback } from 'react';
import {
  Calendar,
  Plus,
  RefreshCw,
  PiggyBank,
  AlertTriangle,
  CheckCircle2,
  Edit2,
  Trash2,
  Layers,
} from 'lucide-react';
import { budgetApi } from '../api/budgetApi';
import { categoryApi } from '../api/categoryApi';
import { Budget, Category } from '../types';
import { formatCurrency } from '../utils/formatters';
import { BudgetModal } from '../components/budgets/BudgetModal';
import { DeleteConfirmModal } from '../components/common/DeleteConfirmModal';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { EmptyState } from '../components/common/EmptyState';

export const BudgetsPage: React.FC = () => {
  const currentDate = new Date();
  const [selectedMonth, setSelectedMonth] = useState<number>(currentDate.getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState<number>(currentDate.getFullYear());

  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Modals
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingBudget, setEditingBudget] = useState<Budget | null>(null);
  const [deleteBudgetId, setDeleteBudgetId] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  const fetchCategories = async () => {
    try {
      const cats = await categoryApi.getCategories();
      setCategories(cats);
    } catch (err: any) {
      console.error('Failed to load categories', err);
    }
  };

  const fetchBudgets = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await budgetApi.getBudgets(selectedMonth, selectedYear);
      setBudgets(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load budgets for the selected period.');
    } finally {
      setLoading(false);
    }
  }, [selectedMonth, selectedYear]);

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchBudgets();
  }, [fetchBudgets]);

  const handleOpenAddModal = () => {
    setEditingBudget(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (budget: Budget) => {
    setEditingBudget(budget);
    setIsModalOpen(true);
  };

  const handleDeleteBudget = async () => {
    if (!deleteBudgetId) return;
    setIsDeleting(true);
    try {
      await budgetApi.deleteBudget(deleteBudgetId);
      setSuccessMessage('Budget removed successfully.');
      setTimeout(() => setSuccessMessage(null), 3000);
      setDeleteBudgetId(null);
      fetchBudgets();
    } catch (err: any) {
      setError(err?.message || 'Failed to delete budget.');
    } finally {
      setIsDeleting(false);
    }
  };

  const overallBudget = budgets.find((b) => b.categoryId === null || b.categoryId === undefined);
  const categoryBudgets = budgets.filter((b) => b.categoryId !== null && b.categoryId !== undefined);
  const configuredCategoryIds: (number | null)[] = budgets.map((b) => (b.categoryId !== undefined ? b.categoryId : null));

  const renderProgressBar = (percentage: number) => {
    const clampedWidth = Math.min(Math.max(percentage, 0), 100);
    let barColor = 'bg-emerald-500';
    if (percentage > 100) {
      barColor = 'bg-rose-500';
    } else if (percentage >= 80) {
      barColor = 'bg-amber-500';
    }

    return (
      <div className="w-full bg-slate-100 dark:bg-slate-800 rounded-full h-2.5 overflow-hidden">
        <div
          className={`h-2.5 rounded-full transition-all duration-500 ${barColor}`}
          style={{ width: `${clampedWidth}%` }}
        />
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Monthly Budgets</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Set and track spending limits for overall expenses and categories
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Period selector */}
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
            onClick={() => fetchBudgets()}
            title="Refresh"
            className="p-2 text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-xs transition-colors cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <button
            onClick={handleOpenAddModal}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 shadow-md shadow-indigo-600/20 transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Set Budget</span>
          </button>
        </div>
      </div>

      {/* Notifications */}
      {successMessage && (
        <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-300 text-sm">
          <CheckCircle2 className="w-4 h-4 text-emerald-500" />
          <span>{successMessage}</span>
        </div>
      )}
      {error && <ErrorAlert message={error} onRetry={fetchBudgets} onDismiss={() => setError(null)} />}

      {loading ? (
        <div className="py-12 flex flex-col items-center justify-center">
          <RefreshCw className="w-6 h-6 text-indigo-600 animate-spin mb-2" />
          <span className="text-xs text-slate-400">Loading budgets...</span>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Overall Monthly Budget Card */}
          <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800">
              <div className="flex items-center gap-3">
                <div className="p-2.5 rounded-xl bg-indigo-100 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
                  <PiggyBank className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-900 dark:text-slate-100">
                    Overall Monthly Budget
                  </h3>
                  <p className="text-xs text-slate-400">Master spending cap for this month</p>
                </div>
              </div>

              {overallBudget && (
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleOpenEditModal(overallBudget)}
                    className="p-1.5 text-slate-400 hover:text-indigo-600 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
                    title="Edit amount"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setDeleteBudgetId(overallBudget.id)}
                    className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
                    title="Delete budget"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>

            {overallBudget ? (
              <div className="pt-5 space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                    <span className="text-xs text-slate-400 block mb-1">Budget Target</span>
                    <span className="text-lg font-bold text-slate-900 dark:text-slate-100">
                      {formatCurrency(overallBudget.amount)}
                    </span>
                  </div>
                  <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                    <span className="text-xs text-slate-400 block mb-1">Actual Spending</span>
                    <span className="text-lg font-bold text-indigo-600 dark:text-indigo-400">
                      {formatCurrency(overallBudget.actualSpending)}
                    </span>
                  </div>
                  <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                    <span className="text-xs text-slate-400 block mb-1">Remaining</span>
                    <span
                      className={`text-lg font-bold ${
                        overallBudget.remainingAmount < 0
                          ? 'text-rose-600 dark:text-rose-400'
                          : 'text-emerald-600 dark:text-emerald-400'
                      }`}
                    >
                      {formatCurrency(overallBudget.remainingAmount)}
                    </span>
                  </div>
                </div>

                <div className="space-y-1.5 pt-2">
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-slate-600 dark:text-slate-400">Budget Utilization</span>
                    <span
                      className={
                        overallBudget.percentageUsed > 100
                          ? 'text-rose-600 font-bold'
                          : 'text-slate-800 dark:text-slate-200'
                      }
                    >
                      {overallBudget.percentageUsed.toFixed(1)}%
                    </span>
                  </div>
                  {renderProgressBar(overallBudget.percentageUsed)}
                </div>

                {overallBudget.percentageUsed > 100 && (
                  <div className="flex items-center gap-2 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300 text-xs font-medium">
                    <AlertTriangle className="w-4 h-4 flex-shrink-0" />
                    <span>
                      Budget limit exceeded by {formatCurrency(Math.abs(overallBudget.remainingAmount))}.
                    </span>
                  </div>
                )}
              </div>
            ) : (
              <div className="pt-6">
                <EmptyState
                  title="No overall budget configured"
                  description="Establish a total spending ceiling for this month to prevent overspending."
                  actionText="Set Overall Budget"
                  onAction={handleOpenAddModal}
                />
              </div>
            )}
          </div>

          {/* Category-Specific Budgets Section */}
          <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
              <div className="flex items-center gap-2">
                <Layers className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                  Category Limits ({categoryBudgets.length})
                </h3>
              </div>
              <button
                onClick={handleOpenAddModal}
                className="text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline cursor-pointer"
              >
                + Add Category Limit
              </button>
            </div>

            {categoryBudgets.length === 0 ? (
              <EmptyState
                title="No category budgets set"
                description="Set specific limits on groceries, dining out, transport, or entertainment to stay on track."
                actionText="Set Category Limit"
                onAction={handleOpenAddModal}
              />
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {categoryBudgets.map((cb) => {
                  const isExceeded = cb.percentageUsed > 100;
                  return (
                    <div
                      key={cb.id}
                      className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/30 space-y-3"
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <h4 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                            {cb.categoryName || 'Category'}
                          </h4>
                          <span className="text-xs text-slate-400">
                            Limit: {formatCurrency(cb.amount)}
                          </span>
                        </div>
                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => handleOpenEditModal(cb)}
                            className="p-1 text-slate-400 hover:text-indigo-600 rounded cursor-pointer"
                            title="Edit limit"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => setDeleteBudgetId(cb.id)}
                            className="p-1 text-slate-400 hover:text-rose-600 rounded cursor-pointer"
                            title="Delete limit"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      <div className="space-y-1.5">
                        <div className="flex justify-between text-xs">
                          <span className="text-slate-500">
                            Spent: <span className="font-semibold text-slate-800 dark:text-slate-200">{formatCurrency(cb.actualSpending)}</span>
                          </span>
                          <span className={isExceeded ? 'text-rose-600 font-bold' : 'text-slate-600 dark:text-slate-400'}>
                            {cb.percentageUsed.toFixed(1)}%
                          </span>
                        </div>
                        {renderProgressBar(cb.percentageUsed)}
                        <div className="flex justify-between text-[11px] text-slate-400 pt-0.5">
                          <span>
                            {cb.remainingAmount >= 0 ? 'Remaining: ' : 'Over by: '}
                            <strong className={isExceeded ? 'text-rose-500' : 'text-emerald-500'}>
                              {formatCurrency(Math.abs(cb.remainingAmount))}
                            </strong>
                          </span>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Budget Modal */}
      <BudgetModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={() => {
          fetchBudgets();
          setSuccessMessage(editingBudget ? 'Budget updated successfully.' : 'Budget configured.');
          setTimeout(() => setSuccessMessage(null), 3000);
        }}
        month={selectedMonth}
        year={selectedYear}
        existingBudget={editingBudget}
        categories={categories}
        existingBudgetCategories={configuredCategoryIds}
      />

      {/* Delete Confirmation */}
      <DeleteConfirmModal
        isOpen={deleteBudgetId !== null}
        onClose={() => setDeleteBudgetId(null)}
        onConfirm={handleDeleteBudget}
        title="Delete Budget"
        message="Are you sure you want to remove this budget ceiling? Your logged expenses will remain unaffected."
        isLoading={isDeleting}
      />
    </div>
  );
};
