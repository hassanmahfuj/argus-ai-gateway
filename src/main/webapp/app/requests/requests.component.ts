import { ChangeDetectionStrategy, Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { IconComponent } from 'app/common/icons/icon.component';
import { RequestsDataService } from './data/requests-data.service';
import { Filters, RequestRow, SortColumn, SortDirection } from './data/requests.model';
import { RequestsTableComponent } from './components/requests-table.component';
import { RequestsPaginationComponent } from './components/requests-pagination.component';
import { RequestDetailDrawerComponent } from './components/request-detail-drawer.component';


/**
 * Full-page Requests table. Owns all client-side state (signals) and derives the
 * visible page via computed signals: filter → sort → paginate. The "Start Live"
 * toggle simulates an incoming feed by prepending generated rows on an interval.
 */
@Component({
  selector: 'app-requests',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IconComponent, RequestsTableComponent, RequestsPaginationComponent, RequestDetailDrawerComponent],
  templateUrl: './requests.component.html',
})
export class RequestsComponent implements OnDestroy {

  private readonly data = inject(RequestsDataService);

  protected readonly rows = signal<RequestRow[]>(this.data.getRequestRows());
  protected readonly sortColumn = signal<SortColumn>('createdAt');
  protected readonly sortDirection = signal<SortDirection>('desc');
  protected readonly currentPage = signal(1);
  protected readonly rowsPerPage = signal(25);
  protected readonly filters = signal<Filters>({});
  protected readonly isLive = signal(false);
  protected readonly selectedRow = signal<RequestRow | null>(null);

  private liveTimer: ReturnType<typeof setInterval> | null = null;

  protected readonly filtered = computed(() => {
    const f = this.filters();
    if (!f.status && !f.model) {
      return this.rows();
    }
    return this.rows().filter((r) =>
      (!f.status || r.status === f.status) && (!f.model || r.model === f.model),
    );
  });

  protected readonly sorted = computed(() => {
    const col = this.sortColumn();
    const dir = this.sortDirection() === 'asc' ? 1 : -1;
    return [...this.filtered()].sort((a, b) => compare(a[col], b[col]) * dir);
  });

  protected readonly totalPages = computed(() => Math.max(1, Math.ceil(this.sorted().length / this.rowsPerPage())));

  protected readonly safePage = computed(() => Math.min(this.currentPage(), this.totalPages()));

  protected readonly paged = computed(() => {
    const size = this.rowsPerPage();
    const start = (this.safePage() - 1) * size;
    return this.sorted().slice(start, start + size);
  });

  protected onSort(col: SortColumn): void {
    if (this.sortColumn() === col) {
      this.sortDirection.update((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortColumn.set(col);
      this.sortDirection.set('asc');
    }
    this.currentPage.set(1);
  }

  protected onModelFilter(model: string): void {
    const current = this.filters();
    this.filters.set(current.model === model ? {} : { model });
    this.currentPage.set(1);
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(Math.min(Math.max(1, page), this.totalPages()));
  }

  protected onRowsPerPage(size: number): void {
    this.rowsPerPage.set(size);
    this.currentPage.set(1);
  }

  protected toggleLive(): void {
    this.isLive() ? this.stopLive() : this.startLive();
  }

  private startLive(): void {
    this.isLive.set(true);
    this.liveTimer = setInterval(() => {
      this.rows.update((r) => [this.data.generateRequestRow(), ...r]);
    }, 4000);
  }

  private stopLive(): void {
    this.isLive.set(false);
    if (this.liveTimer) {
      clearInterval(this.liveTimer);
      this.liveTimer = null;
    }
  }

  ngOnDestroy(): void {
    this.stopLive();
  }

}


/** Numeric-aware ascending comparison for mixed string/number columns. */
function compare(a: unknown, b: unknown): number {
  if (typeof a === 'number' && typeof b === 'number') {
    return a - b;
  }
  return String(a).localeCompare(String(b));
}
