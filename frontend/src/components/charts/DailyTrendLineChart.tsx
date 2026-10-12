import React from 'react';
import { Line } from 'react-chartjs-2';
import './chartSetup';
import { DailyTrend } from '../../types';
import { formatCurrency, formatDate } from '../../utils/formatters';

interface DailyTrendLineChartProps {
  dailyData: DailyTrend[];
}

export const DailyTrendLineChart: React.FC<DailyTrendLineChartProps> = ({ dailyData }) => {
  if (!dailyData || dailyData.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 text-sm text-slate-400">
        No daily spending recorded for this month.
      </div>
    );
  }

  const labels = dailyData.map((d) => formatDate(d.date));
  const amounts = dailyData.map((d) => d.totalSpending);

  const data = {
    labels,
    datasets: [
      {
        label: 'Daily Spending',
        data: amounts,
        fill: true,
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.12)',
        tension: 0.3,
        pointBackgroundColor: '#10b981',
        pointRadius: 3,
        pointHoverRadius: 6,
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
          label: (context: any) => ` Amount: ${formatCurrency(context.raw)}`,
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
            size: 10,
          },
          maxRotation: 45,
          minRotation: 45,
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
      <Line data={data} options={options} />
    </div>
  );
};
