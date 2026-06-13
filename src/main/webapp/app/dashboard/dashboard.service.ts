import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';


export interface DataPoint {
  bucket: string;
  requestCount: number;
  inputTokens: number;
  outputTokens: number;
}

export interface DashboardResponse {
  granularity: string;
  from: string;
  to: string;
  dataPoints: DataPoint[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {

  private http = inject(HttpClient);

  getUsage(granularity: string, from?: string, to?: string): Observable<DashboardResponse> {
    let params = new HttpParams().set('granularity', granularity);
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    return this.http.get<DashboardResponse>(`${environment.apiPath}/api/dashboard/usage`, { params });
  }
}
