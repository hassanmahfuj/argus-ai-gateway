import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { DashboardService, DashboardResponse } from './dashboard.service';


@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  private dashboardService = inject(DashboardService);

  granularities = [
    { value: 'hour', label: 'Hourly' },
    { value: 'day', label: 'Daily' },
    { value: 'week', label: 'Weekly' },
    { value: 'month', label: 'Monthly' }
  ];

  selectedGranularity = 'day';
  loading = false;
  error: string | null = null;
  hasData = false;

  requestChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{
      label: 'Requests',
      data: [],
      borderColor: '#3b82f6',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      fill: true,
      tension: 0.3,
      pointRadius: 3
    }]
  };

  tokenChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        label: 'Input Tokens',
        data: [],
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 3
      },
      {
        label: 'Output Tokens',
        data: [],
        borderColor: '#22c55e',
        backgroundColor: 'rgba(34, 197, 94, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 3
      }
    ]
  };

  chartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      }
    },
    scales: {
      x: {
        display: true,
        ticks: {
          maxTicksLimit: 12,
          maxRotation: 45
        }
      },
      y: {
        display: true,
        beginAtZero: true
      }
    }
  };

  ngOnInit(): void {
    this.loadData();
  }

  changeGranularity(granularity: string): void {
    this.selectedGranularity = granularity;
    this.loadData();
  }

  private loadData(): void {
    this.loading = true;
    this.error = null;
    this.hasData = false;
    this.dashboardService.getUsage(this.selectedGranularity).subscribe({
      next: (response: DashboardResponse) => this.updateCharts(response),
      error: (err) => {
        this.error = 'Failed to load dashboard data';
        this.loading = false;
        console.error('Dashboard error:', err);
      },
      complete: () => this.loading = false
    });
  }

  private updateCharts(response: DashboardResponse): void {
    if (!response.dataPoints || response.dataPoints.length === 0) {
      this.hasData = false;
      return;
    }

    this.hasData = true;
    const labels = response.dataPoints.map(dp => this.formatLabel(dp.bucket));
    const requestCounts = response.dataPoints.map(dp => dp.requestCount);
    const inputTokens = response.dataPoints.map(dp => dp.inputTokens);
    const outputTokens = response.dataPoints.map(dp => dp.outputTokens);

    this.requestChartData = {
      labels,
      datasets: [{
        label: 'Requests',
        data: requestCounts,
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 3
      }]
    };

    this.tokenChartData = {
      labels,
      datasets: [
        {
          label: 'Input Tokens',
          data: inputTokens,
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          fill: true,
          tension: 0.3,
          pointRadius: 3
        },
        {
          label: 'Output Tokens',
          data: outputTokens,
          borderColor: '#22c55e',
          backgroundColor: 'rgba(34, 197, 94, 0.1)',
          fill: true,
          tension: 0.3,
          pointRadius: 3
        }
      ]
    };
  }

  private formatLabel(isoString: string): string {
    const date = new Date(isoString);
    switch (this.selectedGranularity) {
      case 'hour':
        return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
          + ' ' + date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
      case 'day':
        return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
      case 'week':
        return 'W' + this.getWeekNumber(date) + ' ' + date.toLocaleDateString(undefined, { month: 'short' });
      case 'month':
        return date.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
      default:
        return date.toLocaleDateString();
    }
  }

  private getWeekNumber(date: Date): number {
    const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay() || 7));
    const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
    return Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  }
}
