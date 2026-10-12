import React from 'react';
import { Bar } from 'react-chartjs-2';
import './chartSetup';
import { MonthlyTrend } from '../../types';
import { formatCurrency, formatMonthYear } from '../../utils/formatters';

interface MonthlyTrendBarChartProps {
  trends: MonthlyTrend[];
}

export const MonthlyTrendBarChart: React.FC<MonthlyTrendBarChartProps> = ({ trends }) => {
  if (!trends || trends.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 text-sm text-slate-400">
        No monthly trend history available.
      </div>
    );
  }

  const labels = trends.map((item) => item.monthLabel || formatMonthYear(item.month, item.year));
  const dataValues = trends.map((item) => item.totalSpending);

  const data = {
    labels,
    datasets: [
      {
        label: 'Monthly Spending',
        data: dataValues,
        backgroundColor: 'rgba(99, 102, 241, 0.85)',
        hoverBackgroundColor: '#6366f1',
        borderRadius: 6,
        maxBarThickness: 40,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: (context: any) => ` Spending: ${formatCurrency(context.raw)}`,
        },
      },
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
        ticks: {
          font: {
            size: 11,
          },
        },
      },
      y: {
        grid: {
          color: 'rgba(156, 163, 175, 0.15)',
        },
        ticks: {
          font: {
            size: 11,
          },
          callback: (value: any) => `₹${value}`,
        },
      },
    },
  };

  return (
    <div className="relative h-64 w-full">
      <Bar data={data} options={options} />
    </div>
  );
};
