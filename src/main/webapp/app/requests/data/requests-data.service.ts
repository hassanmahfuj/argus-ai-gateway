import { Injectable } from '@angular/core';
import { RequestRow, RequestStatus } from './requests.model';


/**
 * Hardcoded mock provider for the Requests table.
 *
 * Single swap point for a future real implementation: replace the bodies of
 * `getRequestRows()` / `generateRequestRow()` with `HttpClient` calls returning
 * the same shapes (and convert to Observables/signals at that time). No component
 * needs to change.
 */
@Injectable({ providedIn: 'root' })
export class RequestsDataService {

  private readonly models = [
    'gpt-4-1106-vision-preview',
    'gpt-4-vision-preview',
    'gpt-4',
    'gpt-4-0125-preview',
    'gpt-4-turbo-preview',
    'gpt-3.5-turbo-1106',
  ];

  private readonly prompts = [
    'Summarize the quarterly revenue report and highlight outliers',
    'Translate the following legal clause into plain English',
    'Generate a SQL query to find churned users last month',
    'Draft a polite refund email for a delayed shipment',
    'Explain transformer attention to a junior developer',
    'Refactor this Python function to be async and fully typed',
    'Write unit tests for the auth middleware edge cases',
    'Create a marketing tagline for an AI coding assistant',
    'List five counterarguments to the proposed pricing model',
    'Convert this curl command into a Python requests snippet',
  ];

  /** 375 deterministic rows (15 pages of 25) spanning Oct 13 – Nov 12, 2024. */
  getRequestRows(): RequestRow[] {
    const rows: RequestRow[] = [];
    const start = new Date('2024-10-13T12:23:00').getTime();
    const end = new Date('2024-11-12T11:23:00').getTime();
    for (let i = 0; i < 375; i++) {
      const createdAt = new Date(start + ((end - start) * i) / 374).toISOString();
      const status = this.statusFor(i);
      const promptTokens = 120 + (i * 37) % 3800;
      const completionTokens = 40 + (i * 23) % 1800;
      rows.push({
        id: `req_${1000 + i}`,
        createdAt,
        status,
        request: this.prompts[i % this.prompts.length],
        response: status === 'error'
          ? 'Upstream provider returned 503 Service Unavailable'
          : 'OK — response generated with the requested level of detail.',
        model: this.models[i % this.models.length],
        promptTokens,
        completionTokens,
        totalTokens: promptTokens + completionTokens,
      });
    }
    return rows;
  }

  /** One freshly-timestamped row, for the "live" feed simulation. */
  generateRequestRow(): RequestRow {
    const status = this.statusFor(Math.floor(Math.random() * 100));
    const promptTokens = 120 + Math.floor(Math.random() * 3800);
    const completionTokens = 40 + Math.floor(Math.random() * 1800);
    return {
      id: `req_${Date.now()}`,
      createdAt: new Date().toISOString(),
      status,
      request: this.prompts[Math.floor(Math.random() * this.prompts.length)],
      response: status === 'error'
        ? 'Upstream provider returned 503 Service Unavailable'
        : 'OK — response generated with the requested level of detail.',
      model: this.models[Math.floor(Math.random() * this.models.length)],
      promptTokens,
      completionTokens,
      totalTokens: promptTokens + completionTokens,
    };
  }

  /** ~80% success / 12% error / 8% pending, deterministic for a given index. */
  private statusFor(i: number): RequestStatus {
    const m = ((i % 100) + 100) % 100;
    if (m < 80) {
      return 'success';
    }
    if (m < 92) {
      return 'error';
    }
    return 'pending';
  }

}
