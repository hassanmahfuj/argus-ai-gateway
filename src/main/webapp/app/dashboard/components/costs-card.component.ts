import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { CostsMetric } from '../data/dashboard.model';
import { endOnlyScales } from '../charts/chart-plugins';


/** Costs card: KPI + uniform blue bar chart. */
@Component({
  selector: 'app-costs-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BaseChartDirective],
  template: `
    <div class="flex flex-col h-full">
      <span class="text-[13px] text-slate-400 mb-3">Costs</span>
      <div class="text-[28px] font-medium text-slate-900 mb-4">{{ data.kpi }}</div>
      <div class="flex-1" style="height: 170px; position: relative;">
        <canvas baseChart [data]="barData" [options]="barOptions" [type]="'bar'"></canvas>
      </div>
    </div>
  `,
})
export class CostsCardComponent implements OnInit {

  @Input({ required: true }) data!: CostsMetric;

  protected barData!: ChartConfiguration<'bar'>['data'];

  protected readonly barOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: endOnlyScales(),
  };

  ngOnInit(): void {
    this.barData = {
      labels: this.data.labels,
      datasets: [{
        label: 'Cost',
        data: this.data.values,
        backgroundColor: '#93C5FD',
        borderRadius: 4,
        barPercentage: 0.6,
        categoryPercentage: 0.7,
      }],
    };
  }

}
