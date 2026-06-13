import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { IconComponent } from 'app/common/icons/icon.component';
import { CardComponent } from './components/card.component';
import { TimeRangeToggleComponent } from './components/time-range-toggle.component';
import { RequestsCardComponent } from './components/requests-card.component';
import { ErrorsCardComponent } from './components/errors-card.component';
import { TopModelsCardComponent } from './components/top-models-card.component';
import { TokenUsageCardComponent } from './components/token-usage-card.component';
import { CacheHitCardComponent } from './components/cache-hit-card.component';
import { LatencyCardComponent } from './components/latency-card.component';
import { DashboardDataService } from './data/dashboard-data.service';
import { CacheHitMetric, ErrorsMetric, LatencyMetric, RequestsMetric, TokenUsageMetric, TopModel } from './data/dashboard.model';


@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IconComponent,
    CardComponent,
    TimeRangeToggleComponent,
    RequestsCardComponent,
    ErrorsCardComponent,
    TopModelsCardComponent,
    TokenUsageCardComponent,
    CacheHitCardComponent,
    LatencyCardComponent,
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent {

  private readonly data = inject(DashboardDataService);

  protected readonly selectedRange = signal('7D');

  protected readonly requests: RequestsMetric = this.data.getRequests();
  protected readonly errors: ErrorsMetric = this.data.getErrors();
  protected readonly models: TopModel[] = this.data.getTopModels();
  protected readonly tokenUsage: TokenUsageMetric = this.data.getTokenUsage();
  protected readonly cacheHit: CacheHitMetric = this.data.getCacheHit();
  protected readonly latency: LatencyMetric = this.data.getLatency();

  protected selectRange(range: string): void {
    this.selectedRange.set(range);
  }

}
