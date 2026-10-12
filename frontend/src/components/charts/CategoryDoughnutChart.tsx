import React from 'react';
import { Doughnut } from 'react-chartjs-2';
import './chartSetup';
import { CHART_PALETTE } from './chartSetup';
import { CategoryBreakdown } from '../../types';
import { formatCurrency } from '../../utils/formatters';

interface CategoryDoughnutChartProps {
  breakdown: CategoryBreakdown[];
}

export const CategoryDoughnutChart: React.FC<CategoryDoughnutChartProps> = ({ breakdown }) => {
  if (!breakdown || breakdown.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 text-sm text-slate-400">
        No expense data recorded for this period.
      </div>
    );
  }

  const labels = breakdown.map((item) => item.categoryName);
  const dataValues = breakdown.map((item) => item.totalSpending);
  const colors = breakdown.map((_, index) => CHART_PALETTE[index % CHART_PALETTE.length]);

  const data = {
    labels,
    datasets: [
      {
        data: dataValues,
        backgroundColor: colors,
        borderColor: '#ffffff',
        borderWidth: 2,
        hoverOffset: 6,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          boxWidth: 12,
          padding: 16,
          font: {
            size: 11,
          },
          usePointStyle: true,
        },
      },
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const label = context.label || '';
            const value = context.raw || 0;
            const percentage = breakdown[context.dataIndex]?.percentage !== undefined
              ? Number(breakdown[context.dataIndex]?.percentage).toFixed(1)
              : '0';
            return ` ${label}: ${formatCurrency(value)} (${percentage}%)`;
          },
        },
      },
    },
    cutout: '70%',
  };

  return (
    <div className="relative h-64 w-full">
      <Doughnut data={data} options={options} />
    </div>
  );
};
