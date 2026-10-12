import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { Budget, Category } from '../../types';
import { budgetApi } from '../../api/budgetApi';

interface BudgetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  month: number;
  year: number;
  existingBudget?: Budget | null;
  categories: Category[];
  existingBudgetCategories?: (number | null)[];
}

export const BudgetModal: React.FC<BudgetModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  month,
  year,
  existingBudget,
  categories,
  existingBudgetCategories = [],
}) => {
  const [targetType, setTargetType] = useState<'OVERALL' | 'CATEGORY'>('OVERALL');
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
  const [amount, setAmount] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (existingBudget) {
      setAmount(existingBudget.amount.toString());
      if (existingBudget.categoryId) {
        setTargetType('CATEGORY');
        setSelectedCategoryId(existingBudget.categoryId.toString());
      } else {
        setTargetType('OVERALL');
      }
    } else {
      setAmount('');
      // Find first category not already budgeted
      const availableCat = categories.find((c) => !existingBudgetCategories.includes(c.id));
      if (existingBudgetCategories.includes(null) && availableCat) {
        setTargetType('CATEGORY');
        setSelectedCategoryId(availableCat.id.toString());
      } else {
        setTargetType('OVERALL');
        if (availableCat) setSelectedCategoryId(availableCat.id.toString());
      }
    }
    setError(null);
  }, [existingBudget, isOpen, categories, existingBudgetCategories]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Budget amount must be a positive number greater than zero.');
      return;
    }

    setIsSubmitting(true);
    try {
      if (existingBudget) {
        await budgetApi.updateBudget(existingBudget.id, parsedAmount);
      } else {
        const catId = targetType === 'CATEGORY' ? parseInt(selectedCategoryId, 10) : null;
        await budgetApi.createBudget({
          month,
          year,
          amount: parsedAmount,
          categoryId: catId,
        });
      }
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to save budget.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const isEditing = !!existingBudget;
  const overallAlreadyConfigured = !isEditing && existingBudgetCategories.includes(null);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Update Budget' : `Set Budget for ${month}/${year}`}
      maxWidth="sm"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300 text-xs font-medium">
            {error}
          </div>
        )}

        {!isEditing && (
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Budget Target Type
            </label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                disabled={overallAlreadyConfigured}
                onClick={() => setTargetType('OVERALL')}
                className={`py-2 px-3 text-xs font-semibold rounded-xl border transition-all cursor-pointer ${
                  targetType === 'OVERALL'
                    ? 'border-indigo-600 bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400'
                    : 'border-slate-300 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                } ${overallAlreadyConfigured ? 'opacity-40 cursor-not-allowed' : ''}`}
              >
                Overall Monthly
              </button>
              <button
                type="button"
                onClick={() => setTargetType('CATEGORY')}
                className={`py-2 px-3 text-xs font-semibold rounded-xl border transition-all cursor-pointer ${
                  targetType === 'CATEGORY'
                    ? 'border-indigo-600 bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400'
                    : 'border-slate-300 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                }`}
              >
                Category Specific
              </button>
            </div>
            {overallAlreadyConfigured && (
              <p className="mt-1 text-[11px] text-slate-400">
                Overall monthly budget is already configured for this month.
              </p>
            )}
          </div>
        )}

        {targetType === 'CATEGORY' && (
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Select Category
            </label>
            <select
              value={selectedCategoryId}
              onChange={(e) => setSelectedCategoryId(e.target.value)}
              disabled={isEditing}
              required
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 disabled:opacity-60"
            >
              {categories.map((c) => (
                <option
                  key={c.id}
                  value={c.id}
                  disabled={!isEditing && existingBudgetCategories.includes(c.id)}
                >
                  {c.name} {existingBudgetCategories.includes(c.id) && !isEditing ? '(Already set)' : ''}
                </option>
              ))}
            </select>
          </div>
        )}

        <div>
          <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
            Budget Amount (₹) <span className="text-rose-500">*</span>
          </label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            required
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="e.g. 15000"
            className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-800">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-5 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm transition-colors cursor-pointer disabled:opacity-50 flex items-center gap-2"
          >
            {isSubmitting && (
              <span className="w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin" />
            )}
            <span>{isEditing ? 'Update Budget' : 'Save Budget'}</span>
          </button>
        </div>
      </form>
    </Modal>
  );
};
