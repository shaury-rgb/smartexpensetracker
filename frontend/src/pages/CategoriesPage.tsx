import React, { useState, useEffect } from 'react';
import { Plus, Tag, ShieldCheck, UserCheck, Edit2, Trash2, RefreshCw, CheckCircle2 } from 'lucide-react';
import { categoryApi } from '../api/categoryApi';
import { Category } from '../types';
import { CategoryModal } from '../components/categories/CategoryModal';
import { DeleteConfirmModal } from '../components/common/DeleteConfirmModal';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { EmptyState } from '../components/common/EmptyState';

export const CategoriesPage: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Modals
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [deleteCategoryId, setDeleteCategoryId] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  const fetchCategories = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await categoryApi.getCategories();
      setCategories(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load categories.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const handleCreateOpen = () => {
    setEditingCategory(null);
    setIsModalOpen(true);
  };

  const handleEditOpen = (cat: Category) => {
    setEditingCategory(cat);
    setIsModalOpen(true);
  };

  const handleDeleteCategory = async () => {
    if (!deleteCategoryId) return;
    setIsDeleting(true);
    setError(null);
    try {
      await categoryApi.deleteCategory(deleteCategoryId);
      setSuccessMessage('Category deleted successfully.');
      setTimeout(() => setSuccessMessage(null), 3000);
      setDeleteCategoryId(null);
      fetchCategories();
    } catch (err: any) {
      setError(err?.message || 'Could not delete category. It may be currently used by existing expenses.');
      setDeleteCategoryId(null);
    } finally {
      setIsDeleting(false);
    }
  };

  const systemCategories = categories.filter((c) => c.isSystem);
  const userCategories = categories.filter((c) => !c.isSystem);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Categories</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Organize transactions with system and custom categories
          </p>
        </div>

        <button
          onClick={handleCreateOpen}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 shadow-md shadow-indigo-600/20 transition-all cursor-pointer self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>New Category</span>
        </button>
      </div>

      {/* Notifications */}
      {successMessage && (
        <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-300 text-sm">
          <CheckCircle2 className="w-4 h-4 text-emerald-500" />
          <span>{successMessage}</span>
        </div>
      )}
      {error && <ErrorAlert message={error} onRetry={fetchCategories} onDismiss={() => setError(null)} />}

      {/* Grid of Categories */}
      {loading ? (
        <div className="py-12 flex flex-col items-center justify-center">
          <RefreshCw className="w-6 h-6 text-indigo-600 animate-spin mb-2" />
          <span className="text-xs text-slate-400">Loading categories...</span>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Custom User Categories */}
          <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
              <div className="flex items-center gap-2">
                <UserCheck className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                  Custom Categories ({userCategories.length})
                </h3>
              </div>
              <span className="text-xs text-slate-400">Personal to your account</span>
            </div>

            {userCategories.length === 0 ? (
              <EmptyState
                title="No custom categories yet"
                description="Create custom categories to tailor the expense tracker to your lifestyle."
                actionText="Create Category"
                onAction={handleCreateOpen}
              />
            ) : (
              <div className="space-y-2">
                {userCategories.map((cat) => (
                  <div
                    key={cat.id}
                    className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50 hover:bg-slate-100/60 dark:hover:bg-slate-800 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className="p-2 rounded-lg bg-indigo-100 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
                        <Tag className="w-4 h-4" />
                      </div>
                      <span className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                        {cat.name}
                      </span>
                    </div>

                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => handleEditOpen(cat)}
                        className="p-1.5 text-slate-400 hover:text-indigo-600 rounded-lg hover:bg-white dark:hover:bg-slate-700 transition-colors cursor-pointer"
                        title="Rename category"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => setDeleteCategoryId(cat.id)}
                        className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-white dark:hover:bg-slate-700 transition-colors cursor-pointer"
                        title="Delete category"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* System Default Categories */}
          <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
                <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">
                  System Categories ({systemCategories.length})
                </h3>
              </div>
              <span className="text-xs text-slate-400">Standard defaults</span>
            </div>

            <div className="space-y-2">
              {systemCategories.map((cat) => (
                <div
                  key={cat.id}
                  className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50"
                >
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg bg-emerald-100 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400">
                      <Tag className="w-4 h-4" />
                    </div>
                    <span className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                      {cat.name}
                    </span>
                  </div>
                  <span className="text-[11px] font-medium text-slate-400 px-2 py-0.5 rounded bg-slate-200/60 dark:bg-slate-700/60">
                    Protected
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Category Modal */}
      <CategoryModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={() => {
          fetchCategories();
          setSuccessMessage(editingCategory ? 'Category renamed successfully.' : 'Custom category created.');
          setTimeout(() => setSuccessMessage(null), 3000);
        }}
        initialCategory={editingCategory}
      />

      {/* Delete Confirmation */}
      <DeleteConfirmModal
        isOpen={deleteCategoryId !== null}
        onClose={() => setDeleteCategoryId(null)}
        onConfirm={handleDeleteCategory}
        title="Delete Custom Category"
        message="Are you sure you want to delete this category? If any expenses are currently assigned to this category, deletion will be rejected to protect data integrity."
        isLoading={isDeleting}
      />
    </div>
  );
};
