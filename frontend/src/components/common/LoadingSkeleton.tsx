import React from 'react';

interface LoadingSkeletonProps {
  className?: string;
  count?: number;
}

export const LoadingSkeleton: React.FC<LoadingSkeletonProps> = ({ className = 'h-6 w-full', count = 1 }) => {
  return (
    <div className="space-y-3 animate-pulse">
      {Array.from({ length: count }).map((_, i) => (
        <div
          key={i}
          className={`bg-slate-200 dark:bg-slate-800 rounded-lg ${className}`}
        />
      ))}
    </div>
  );
};

export const CardSkeleton: React.FC = () => {
  return (
    <div className="p-6 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 animate-pulse space-y-4">
      <div className="flex justify-between items-center">
        <div className="h-4 w-28 bg-slate-200 dark:bg-slate-800 rounded"></div>
        <div className="h-8 w-8 bg-slate-200 dark:bg-slate-800 rounded-full"></div>
      </div>
      <div className="h-8 w-36 bg-slate-200 dark:bg-slate-800 rounded"></div>
      <div className="h-3 w-48 bg-slate-200 dark:bg-slate-800 rounded"></div>
    </div>
  );
};
