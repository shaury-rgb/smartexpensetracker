import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { StatCard } from '../components/common/StatCard';
import { IndianRupee } from 'lucide-react';
import { EmptyState } from '../components/common/EmptyState';
import { formatCurrency, formatDate, formatMonthYear } from '../utils/formatters';

describe('Frontend Utility Formatters', () => {
  it('formats INR currency correctly', () => {
    const formatted = formatCurrency(1250.5);
    expect(formatted).toContain('1,250.50');
    expect(formatted).toContain('₹');
  });

  it('handles null or undefined currency gracefully', () => {
    expect(formatCurrency(null)).toBe('₹0.00');
    expect(formatCurrency(undefined)).toBe('₹0.00');
  });

  it('formats dates consistently', () => {
    expect(formatDate('2026-10-12')).toBe('12 Oct 2026');
    expect(formatDate(null)).toBe('—');
  });

  it('formats month and year', () => {
    expect(formatMonthYear(10, 2026)).toBe('Oct 2026');
  });
});

describe('Common UI Components', () => {
  it('renders StatCard with title, value, and subtitle', () => {
    render(
      <StatCard
        title="Total Spending"
        value="₹15,000.00"
        subtitle="12 transactions logged"
        icon={IndianRupee}
        color="blue"
      />
    );
    expect(screen.getByText('Total Spending')).toBeInTheDocument();
    expect(screen.getByText('₹15,000.00')).toBeInTheDocument();
    expect(screen.getByText('12 transactions logged')).toBeInTheDocument();
  });

  it('renders EmptyState with action button', () => {
    render(
      <EmptyState
        title="No expenses found"
        description="Record your first transaction"
        actionText="Add Expense"
        onAction={() => {}}
      />
    );
    expect(screen.getByText('No expenses found')).toBeInTheDocument();
    expect(screen.getByText('Record your first transaction')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add Expense' })).toBeInTheDocument();
  });
});
