import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, Plugin } from 'chart.js';
import { ErrorsMetric } from '../data/dashboard.model';
import { createDonutCenterPlugin } from '../charts/chart-plugins';


/** Errors card: KPI-less donut with center text + segment legend. */
@Component({
  selector: 'app-errors-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, BaseChartDirective],
  template: `
    <div class="flex flex-col h-full">
      <span class="text-[13px] text-slate-400 mb-4">Errors</span>
      <div class="flex items-center gap-5 flex-1">
        <div class="relative shrink-0" style="width: 160px; height: 160px;">
          <canvas baseChart [data]="doughnutData" [options]="doughnutOptions" [plugins]="plugins" [type]="'doughnut'"></canvas>
        </div>
        <div class="flex flex-col gap-2.5 min-w-0">
          @for (seg of data.segments; track seg.code) {
            <div class="flex items-center gap-2 text-[13px]">
              <span class="w-2 h-2 rounded-full shrink-0" [style.background]="seg.color"></span>
              <span class="text-slate-600">{{ seg.code }}</span>
              <span class="text-slate-400 ml-auto tabular-nums">{{ seg.value | number }}</span>
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class ErrorsCardComponent implements OnInit {

  @Input({ required: true }) data!: ErrorsMetric;

  protected doughnutData!: ChartConfiguration<'doughnut'>['data'];
  protected plugins: Plugin<'doughnut'>[] = [];

  protected readonly doughnutOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '70%',
    plugins: { legend: { display: false } },
  };

  ngOnInit(): void {
    this.doughnutData = {
      labels: this.data.segments.map((s) => s.code),
      datasets: [{
        data: this.data.segments.map((s) => s.value),
        backgroundColor: this.data.segments.map((s) => s.color),
        borderColor: '#ffffff',
        borderWidth: 2,
      }],
    };
    this.plugins = [
      createDonutCenterPlugin(() => ({ total: this.data.total, label: this.data.totalLabel })),
    ];
  }

}
