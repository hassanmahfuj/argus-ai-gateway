import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IconComponent } from 'app/common/icons/icon.component';
import { RequestRow, RequestStatus, SortColumn, SortDirection } from '../data/requests.model';


interface Column {
  key: SortColumn;
  label: string;
  align: 'left' | 'right';
}


/** Sortable, row-clickable table of request rows. All state lives in the page. */
@Component({
  selector: 'app-requests-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IconComponent],
  template: `
    <div class="overflow-x-auto">
      <table class="w-full border-collapse">
        <thead>
          <tr>
            @for (col of columns; track col.key) {
              <th (click)="onSort(col.key)"
                  class="px-2 py-[7px] text-[11px] font-medium text-slate-500 border-b border-slate-200 whitespace-nowrap cursor-pointer select-none"
                  [class.text-right]="col.align === 'right'">
                <span class="inline-flex items-center gap-1" [class.flex-row-reverse]="col.align === 'right'">
                  {{ col.label }}
                  <app-icon [name]="sortIcon(col.key) ?? 'chevron-down'" [size]="12"
                            [class.text-slate-700]="sortIcon(col.key) !== null"
                            [class.text-slate-300]="sortIcon(col.key) === null" />
                </span>
              </th>
            }
          </tr>
        </thead>
        <tbody>
          @for (row of rows; track row.id) {
            <tr (click)="rowSelect.emit(row)" class="h-10 hover:bg-slate-50 cursor-pointer">
              <td class="px-2 py-[7px] align-middle text-xs text-slate-600 whitespace-nowrap border-b border-slate-200">
                {{ row.createdAt | date: 'MMM d, y h:mm a' }}
              </td>
              <td class="px-2 py-[7px] align-middle border-b border-slate-200">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium capitalize" [class]="statusClass(row.status)">
                  {{ row.status }}
                </span>
              </td>
              <td class="px-2 py-[7px] align-middle border-b border-slate-200">
                <span class="block max-w-[130px] truncate text-xs text-slate-500" [title]="row.request">{{ row.request }}</span>
              </td>
              <td class="px-2 py-[7px] align-middle border-b border-slate-200">
                <span class="block max-w-[130px] truncate text-xs text-slate-500" [title]="row.response">{{ row.response }}</span>
              </td>
              <td class="px-2 py-[7px] align-middle border-b border-slate-200">
                <button type="button" (click)="onModelClick($event, row.model)"
                        class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-[#F3F0FF] text-[#5B21B6] hover:brightness-95 cursor-pointer">
                  {{ row.model }}
                </button>
              </td>
              <td class="px-2 py-[7px] align-middle text-right text-xs text-slate-700 tabular-nums border-b border-slate-200">{{ row.totalTokens }}</td>
              <td class="px-2 py-[7px] align-middle text-right text-xs text-slate-700 tabular-nums border-b border-slate-200">{{ row.promptTokens }}</td>
              <td class="px-2 py-[7px] align-middle text-right text-xs text-slate-700 tabular-nums border-b border-slate-200">{{ row.completionTokens }}</td>
            </tr>
          } @empty {
            <tr>
              <td class="px-2 py-10 text-center text-sm text-slate-400" [attr.colspan]="columns.length">
                No requests match the current filters.
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class RequestsTableComponent {

  @Input({ required: true }) rows!: RequestRow[];
  @Input({ required: true }) sortColumn!: SortColumn;
  @Input({ required: true }) sortDirection!: SortDirection;

  @Output() sortChange = new EventEmitter<SortColumn>();
  @Output() rowSelect = new EventEmitter<RequestRow>();
  @Output() modelFilter = new EventEmitter<string>();

  protected readonly columns: Column[] = [
    { key: 'createdAt', label: 'Created At', align: 'left' },
    { key: 'status', label: 'Status', align: 'left' },
    { key: 'request', label: 'Request', align: 'left' },
    { key: 'response', label: 'Response', align: 'left' },
    { key: 'model', label: 'Model', align: 'left' },
    { key: 'totalTokens', label: 'Total Tokens', align: 'right' },
    { key: 'promptTokens', label: 'Prompt Tokens', align: 'right' },
    { key: 'completionTokens', label: 'Completion Tokens', align: 'right' },
  ];

  protected sortIcon(col: SortColumn): 'chevron-up' | 'chevron-down' | null {
    if (col !== this.sortColumn) {
      return null;
    }
    return this.sortDirection === 'asc' ? 'chevron-up' : 'chevron-down';
  }

  protected onSort(col: SortColumn): void {
    this.sortChange.emit(col);
  }

  protected statusClass(status: RequestStatus): string {
    switch (status) {
      case 'success': return 'bg-[#DCFCE7] text-[#166534]';
      case 'error': return 'bg-[#FEE2E2] text-[#991B1B]';
      case 'pending': return 'bg-[#FEF3C7] text-[#92400E]';
    }
  }

  protected onModelClick(event: Event, model: string): void {
    event.stopPropagation();
    this.modelFilter.emit(model);
  }

}
