import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { TokenUsageMetric } from '../data/dashboard.model';
import { endOnlyScales } from '../charts/chart-plugins';


/** Token Usage card: KPI + stacked bar of prompt vs completion tokens over time. */
@Component({
  selector: 'app-token-usage-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BaseChartDirective],
  template: `
    <div class="flex flex-col h-full">
      <div class="flex items-center justify-between mb-3">
        <span class="text-[13px] font-medium text-slate-500">Token Usage</span>
        <div class="flex items-center gap-3">
          <span class="inline-flex items-center gap-1.5 text-xs text-slate-500">
            <span class="w-2 h-2 rounded-full bg-blue-500"></span>prompt
          </span>
          <span class="inline-flex items-center gap-1.5 text-xs text-slate-500">
            <span class="w-2 h-2 rounded-full bg-blue-300"></span>completion
          </span>
        </div>
      </div>
      <div class="text-[28px] font-medium text-slate-900 mb-4">{{ data.kpi }}</div>
      <div class="flex-1" style="height: 170px; position: relative;">
        <canvas baseChart [data]="barData" [options]="barOptions" [type]="'bar'"></canvas>
      </div>
    </div>
  `,
})
export class TokenUsageCardComponent implements OnInit {

  @Input({ required: true }) data!: TokenUsageMetric;

  protected barData!: ChartConfiguration<'bar'>['data'];

  protected readonly barOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: endOnlyScales({ stacked: true }),
  };

  ngOnInit(): void {
    this.barData = {
      labels: this.data.labels,
      datasets: [
        {
          label: 'Prompt',
          data: this.data.prompt,
          backgroundColor: '#3B82F6',
          borderRadius: 4,
          barPercentage: 0.6,
          categoryPercentage: 0.7,
        },
        {
          label: 'Completion',
          data: this.data.completion,
          backgroundColor: '#93C5FD',
          borderRadius: 4,
          barPercentage: 0.6,
          categoryPercentage: 0.7,
        },
      ],
    };
  }

}
