import { Injectable } from '@angular/core';
import {
  CostsMetric,
  ErrorsMetric,
  LatencyMetric,
  RequestsMetric,
  TopCountry,
  TopModel,
} from './dashboard.model';


/**
 * Hardcoded mock provider for the analytics dashboard.
 *
 * This is the single swap point for a future real implementation: replace the
 * bodies of these getters with `HttpClient` calls returning the same interfaces
 * (and convert to Observables/signals at that time). No component needs to change.
 */
@Injectable({ providedIn: 'root' })
export class DashboardDataService {

  getRequests(): RequestsMetric {
    return {
      kpi: '3,310,278',
      labels: ['22 Feb', '1 Mar', '8 Mar', '15 Mar', '22 Mar', '29 Mar', '5 Apr', '12 Apr', '19 Apr', '26 Apr', '3 May', '23 May'],
      success: [272000, 268000, 281000, 275000, 290000, 284000, 278000, 295000, 289000, 283000, 291000, 286000],
      error: [16200, 14800, 17500, 16100, 18600, 17300, 19800, 18400, 17700, 16200, 18900, 16500],
    };
  }

  getErrors(): ErrorsMetric {
    return {
      total: '4,273',
      totalLabel: 'Total Errors',
      segments: [
        { code: '400', value: 3500, color: '#38BDF8' },
        { code: '401', value: 500, color: '#2DD4BF' },
        { code: '500', value: 273, color: '#0EA5E9' },
      ],
    };
  }

  getTopModels(): TopModel[] {
    return [
      { name: 'gpt-4-1106-vision-preview', count: '1,204,530' },
      { name: 'gpt-4-vision-preview', count: '876,210' },
      { name: 'gpt-4', count: '642,980' },
      { name: 'gpt-4-0125-preview', count: '421,775' },
      { name: 'gpt-4-turbo-preview', count: '198,440' },
      { name: 'gpt-3.5-turbo-1106', count: '87,120' },
    ];
  }

  getCosts(): CostsMetric {
    return {
      kpi: '$93,128.22',
      labels: ['22 Feb', '1 Mar', '8 Mar', '15 Mar', '22 Mar', '29 Mar', '5 Apr', '12 Apr', '19 Apr', '23 May'],
      values: [8200, 9100, 8800, 9400, 8600, 9300, 9700, 8900, 9500, 9200],
    };
  }

  getTopCountries(): TopCountry[] {
    return [
      { flag: '🇺🇸', name: 'United States', iso: 'US', count: '1,402,330' },
      { flag: '🇬🇧', name: 'United Kingdom', iso: 'GB', count: '612,450' },
      { flag: '🇩🇪', name: 'Germany', iso: 'DE', count: '487,910' },
      { flag: '🇯🇵', name: 'Japan', iso: 'JP', count: '399,080' },
      { flag: '🇧🇷', name: 'Brazil', iso: 'BR', count: '263,715' },
    ];
  }

  getLatency(): LatencyMetric {
    return {
      kpi: '6.058',
      kpiUnit: 's / req',
      labels: ['22 Feb', '1 Mar', '8 Mar', '15 Mar', '22 Mar', '29 Mar', '5 Apr', '12 Apr', '19 Apr', '26 Apr', '3 May', '23 May'],
      values: [5.2, 5.8, 6.1, 5.5, 6.4, 5.9, 6.2, 6.7, 6.0, 5.6, 6.3, 6.058],
      band: { from: 5.8, to: 6.5 },
    };
  }

}
