import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { RequestsMetric } from '../data/dashboard.model';
import { endOnlyScales } from '../charts/chart-plugins';


/** Requests card: KPI + success/error dual-line chart. */
@Component({
  selector: 'app-requests-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BaseChartDirective],
  template: `
    <div class="flex flex-col h-full">
      <div class="flex items-center justify-between mb-3">
        <span class="text-[13px] text-slate-400">Requests</span>
        <div class="flex items-center gap-3">
          <span class="inline-flex items-center gap-1.5 text-xs text-slate-500">
            <span class="w-2 h-2 rounded-full bg-green-500"></span>success
          </span>
          <span class="inline-flex items-center gap-1.5 text-xs text-slate-500">
            <span class="w-2 h-2 rounded-full bg-red-500"></span>error
          </span>
        </div>
      </div>
      <div class="text-[28px] font-medium text-slate-900 mb-4">{{ data.kpi }}</div>
      <div class="flex-1" style="height: 180px; position: relative;">
        <canvas baseChart [data]="lineData" [options]="lineOptions" [type]="'line'"></canvas>
      </div>
    </div>
  `,
})
export class RequestsCardComponent implements OnInit {

  @Input({ required: true }) data!: RequestsMetric;

  protected lineData!: ChartConfiguration<'line'>['data'];

  protected readonly lineOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: endOnlyScales(),
    elements: { point: { radius: 0 } },
  };

  ngOnInit(): void {
    this.lineData = {
      labels: this.data.labels,
      datasets: [
        {
          label: 'success',
          data: this.data.success,
          borderColor: '#22C55E',
          fill: false,
          tension: 0.35,
          borderWidth: 2,
          pointRadius: 0,
        },
        {
          label: 'error',
          data: this.data.error,
          borderColor: '#EF4444',
          fill: false,
          tension: 0.35,
          borderWidth: 2,
          pointRadius: 0,
        },
      ],
    };
  }

}
