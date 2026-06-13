import { ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IconComponent } from 'app/common/icons/icon.component';
import { RequestRow, RequestStatus } from '../data/requests.model';


/** Right-side slide-over showing a single request's full detail. Closes on Esc / backdrop click. */
@Component({
  selector: 'app-request-detail-drawer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IconComponent],
  template: `
    @if (row) {
      <div class="fixed inset-0 z-40 flex justify-end" role="presentation">
        <div class="absolute inset-0 bg-slate-900/40" (click)="close.emit()"></div>

        <div role="dialog" aria-modal="true" [attr.aria-label]="'Request ' + row.id"
             class="request-drawer-panel relative h-full w-full max-w-[420px] bg-white shadow-xl flex flex-col">
          <!-- Header -->
          <div class="flex items-center justify-between px-5 h-14 border-b border-slate-200 shrink-0">
            <div class="flex items-center gap-2 min-w-0">
              <span class="text-sm font-semibold text-slate-900">Request</span>
              <span class="text-xs text-slate-400 truncate">{{ row.id }}</span>
            </div>
            <button type="button" (click)="close.emit()" aria-label="Close detail"
                    class="w-8 h-8 inline-flex items-center justify-center rounded-md text-slate-400 hover:bg-slate-100 cursor-pointer shrink-0">
              <app-icon name="close" [size]="18" />
            </button>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto p-5 space-y-5 text-sm">
            <div class="flex items-center gap-2">
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium capitalize" [class]="statusClass(row.status)">
                {{ row.status }}
              </span>
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-[#F3F0FF] text-[#5B21B6]">
                {{ row.model }}
              </span>
            </div>

            <dl class="space-y-4">
              <div>
                <dt class="text-[11px] font-medium uppercase tracking-wide text-slate-400 mb-1">Created At</dt>
                <dd class="text-slate-700">{{ row.createdAt | date: 'MMM d, y h:mm:ss a' }}</dd>
              </div>

              <div class="grid grid-cols-3 gap-3">
                <div>
                  <dt class="text-[11px] font-medium uppercase tracking-wide text-slate-400 mb-1">Total</dt>
                  <dd class="text-slate-700 tabular-nums">{{ row.totalTokens }}</dd>
                </div>
                <div>
                  <dt class="text-[11px] font-medium uppercase tracking-wide text-slate-400 mb-1">Prompt</dt>
                  <dd class="text-slate-700 tabular-nums">{{ row.promptTokens }}</dd>
                </div>
                <div>
                  <dt class="text-[11px] font-medium uppercase tracking-wide text-slate-400 mb-1">Completion</dt>
                  <dd class="text-slate-700 tabular-nums">{{ row.completionTokens }}</dd>
                </div>
              </div>

              <div>
                <dt class="text-[11px] font-medium uppercase tracking-wide text-slate-400 mb-1">Request</dt>
                <dd class="text-slate-700 bg-slate-50 rounded-lg p-3 break-words">{{ row.request }}</dd>
              </div>
              <div>
                <dt class="text-[11px] font-medium uppercase tracking-wide text-slate-400 mb-1">Response</dt>
                <dd class="text-slate-700 bg-slate-50 rounded-lg p-3 break-words">{{ row.response }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>
    }
  `,
})
export class RequestDetailDrawerComponent {

  @Input() row: RequestRow | null = null;

  @Output() close = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.row) {
      this.close.emit();
    }
  }

  protected statusClass(status: RequestStatus): string {
    switch (status) {
      case 'success': return 'bg-[#DCFCE7] text-[#166534]';
      case 'error': return 'bg-[#FEE2E2] text-[#991B1B]';
      case 'pending': return 'bg-[#FEF3C7] text-[#92400E]';
    }
  }

}
