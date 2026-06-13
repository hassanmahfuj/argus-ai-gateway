import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, Plugin } from 'chart.js';
import { LatencyMetric } from '../data/dashboard.model';
import { createRefBandPlugin, endOnlyScales } from '../charts/chart-plugins';


/** Latency card: KPI (with unit) + teal area chart with a shaded reference band. */
@Component({
  selector: 'app-latency-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BaseChartDirective],
  template: `
    <div class="flex flex-col h-full">
      <span class="text-[13px] font-medium text-slate-500 mb-3">Latency</span>
      <div class="mb-4">
        <span class="text-[28px] font-medium text-slate-900">{{ data.kpi }}</span>
        <span class="text-sm text-slate-500 ml-1">{{ data.kpiUnit }}</span>
      </div>
      <div class="flex-1" style="height: 170px; position: relative;">
        <canvas baseChart [data]="lineData" [options]="lineOptions" [plugins]="plugins" [type]="'line'"></canvas>
      </div>
    </div>
  `,
})
export class LatencyCardComponent implements OnInit {

  @Input({ required: true }) data!: LatencyMetric;

  protected lineData!: ChartConfiguration<'line'>['data'];
  protected plugins: Plugin<'line'>[] = [];

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
      datasets: [{
        label: 'Latency',
        data: this.data.values,
        borderColor: '#14B8A6',
        backgroundColor: 'rgba(20,184,166,0.12)',
        fill: true,
        tension: 0.35,
        borderWidth: 2,
        pointRadius: 0,
      }],
    };
    this.plugins = [
      createRefBandPlugin(() => ({
        from: this.data.band.from,
        to: this.data.band.to,
        color: 'rgba(20,184,166,0.08)',
      })),
    ];
  }

}
