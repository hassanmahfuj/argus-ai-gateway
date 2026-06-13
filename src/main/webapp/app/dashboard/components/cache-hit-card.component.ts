import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, Plugin } from 'chart.js';
import { CacheHitMetric } from '../data/dashboard.model';
import { createDonutCenterPlugin } from '../charts/chart-plugins';


/** Cache Hit Rate card: doughnut gauge (hit/miss) with a centered percentage. */
@Component({
  selector: 'app-cache-hit-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BaseChartDirective],
  template: `
    <div class="flex flex-col h-full">
      <span class="text-[13px] font-medium text-slate-500 mb-4">Cache Hit Rate</span>
      <div class="flex items-center gap-5 flex-1">
        <div class="relative shrink-0" style="width: 160px; height: 160px;">
          <canvas baseChart [data]="doughnutData" [options]="doughnutOptions" [plugins]="plugins" [type]="'doughnut'"></canvas>
        </div>
        <div class="flex flex-col gap-2.5 min-w-0">
          <div class="flex items-center gap-2 text-[13px]">
            <span class="w-2 h-2 rounded-full shrink-0 bg-green-500"></span>
            <span class="text-slate-600">Hit</span>
            <span class="text-slate-500 ml-auto tabular-nums">{{ data.hit }}%</span>
          </div>
          <div class="flex items-center gap-2 text-[13px]">
            <span class="w-2 h-2 rounded-full shrink-0 bg-slate-300"></span>
            <span class="text-slate-600">Miss</span>
            <span class="text-slate-500 ml-auto tabular-nums">{{ data.miss }}%</span>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class CacheHitCardComponent implements OnInit {

  @Input({ required: true }) data!: CacheHitMetric;

  protected doughnutData!: ChartConfiguration<'doughnut'>['data'];
  protected plugins: Plugin<'doughnut'>[] = [];

  protected readonly doughnutOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '72%',
    plugins: { legend: { display: false } },
  };

  ngOnInit(): void {
    this.doughnutData = {
      labels: ['Hit', 'Miss'],
      datasets: [{
        data: [this.data.hit, this.data.miss],
        backgroundColor: ['#22C55E', '#CBD5E1'],
        borderColor: '#ffffff',
        borderWidth: 2,
      }],
    };
    this.plugins = [
      createDonutCenterPlugin(() => ({ total: this.data.kpi, label: this.data.kpiLabel })),
    ];
  }

}
