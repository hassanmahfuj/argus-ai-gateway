import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { IconComponent } from 'app/common/icons/icon.component';
import { CardComponent } from './components/card.component';
import { TimeRangeToggleComponent } from './components/time-range-toggle.component';
import { RequestsCardComponent } from './components/requests-card.component';
import { ErrorsCardComponent } from './components/errors-card.component';
import { TopModelsCardComponent } from './components/top-models-card.component';
import { CostsCardComponent } from './components/costs-card.component';
import { TopCountriesCardComponent } from './components/top-countries-card.component';
import { LatencyCardComponent } from './components/latency-card.component';
import { DashboardDataService } from './data/dashboard-data.service';
import { CostsMetric, ErrorsMetric, LatencyMetric, RequestsMetric, TopCountry, TopModel } from './data/dashboard.model';


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
    CostsCardComponent,
    TopCountriesCardComponent,
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
  protected readonly costs: CostsMetric = this.data.getCosts();
  protected readonly countries: TopCountry[] = this.data.getTopCountries();
  protected readonly latency: LatencyMetric = this.data.getLatency();

  protected selectRange(range: string): void {
    this.selectedRange.set(range);
  }

}
