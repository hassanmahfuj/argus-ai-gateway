import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { IconComponent } from 'app/common/icons/icon.component';


/** Footer: rows-per-page select (left) + first/prev/next/last + "page of total" (right). */
@Component({
  selector: 'app-requests-pagination',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IconComponent],
  template: `
    <div class="flex items-center justify-between gap-4 px-4 py-3 text-xs text-slate-500">
      <!-- Left: rows per page -->
      <div class="flex items-center gap-2">
        <span>Row</span>
        <select (change)="onRowsPerPage($event)"
                class="rounded-md border border-slate-200 bg-white py-1 pl-2 pr-7 text-xs text-slate-700 cursor-pointer">
          @for (opt of pageOptions; track opt) {
            <option [value]="opt" [selected]="opt === rowsPerPage">{{ opt }}</option>
          }
        </select>
      </div>

      <!-- Right: nav buttons + page indicator -->
      <div class="flex items-center gap-3">
        <span>
          <span class="font-medium text-slate-700">{{ currentPage }}</span>
          of
          <span class="text-slate-400">{{ totalPages }}</span>
        </span>
        <div class="flex items-center gap-1">
          <button type="button" (click)="pageChange.emit(1)" [disabled]="currentPage <= 1"
                  aria-label="First page"
                  class="w-[26px] h-[26px] inline-flex items-center justify-center rounded-md border border-slate-200 text-slate-500 hover:bg-slate-100 disabled:opacity-40 disabled:hover:bg-white disabled:cursor-not-allowed cursor-pointer">
            <app-icon name="chevrons-left" [size]="14" />
          </button>
          <button type="button" (click)="pageChange.emit(currentPage - 1)" [disabled]="currentPage <= 1"
                  aria-label="Previous page"
                  class="w-[26px] h-[26px] inline-flex items-center justify-center rounded-md border border-slate-200 text-slate-500 hover:bg-slate-100 disabled:opacity-40 disabled:hover:bg-white disabled:cursor-not-allowed cursor-pointer">
            <app-icon name="chevron-left" [size]="14" />
          </button>
          <button type="button" (click)="pageChange.emit(currentPage + 1)" [disabled]="currentPage >= totalPages"
                  aria-label="Next page"
                  class="w-[26px] h-[26px] inline-flex items-center justify-center rounded-md border border-slate-200 text-slate-500 hover:bg-slate-100 disabled:opacity-40 disabled:hover:bg-white disabled:cursor-not-allowed cursor-pointer">
            <app-icon name="chevron-right" [size]="14" />
          </button>
          <button type="button" (click)="pageChange.emit(totalPages)" [disabled]="currentPage >= totalPages"
                  aria-label="Last page"
                  class="w-[26px] h-[26px] inline-flex items-center justify-center rounded-md border border-slate-200 text-slate-500 hover:bg-slate-100 disabled:opacity-40 disabled:hover:bg-white disabled:cursor-not-allowed cursor-pointer">
            <app-icon name="chevrons-right" [size]="14" />
          </button>
        </div>
      </div>
    </div>
  `,
})
export class RequestsPaginationComponent {

  @Input({ required: true }) currentPage!: number;
  @Input({ required: true }) totalPages!: number;
  @Input({ required: true }) rowsPerPage!: number;

  @Output() pageChange = new EventEmitter<number>();
  @Output() rowsPerPageChange = new EventEmitter<number>();

  protected readonly pageOptions = [25, 50, 100];

  protected onRowsPerPage(event: Event): void {
    const value = Number((event.target as HTMLSelectElement).value);
    this.rowsPerPageChange.emit(value);
  }

}
