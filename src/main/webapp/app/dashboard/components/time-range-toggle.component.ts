import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';


/** Segmented time-range pill control (Custom / 24H / 7D / 1M / 3M). */
@Component({
  selector: 'app-time-range-toggle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="inline-flex items-center gap-1">
      @for (range of ranges; track range) {
        <button type="button" (click)="select(range)" [class]="buttonClass(range)"
                [attr.aria-pressed]="value === range">
          {{ range }}
        </button>
      }
    </div>
  `,
})
export class TimeRangeToggleComponent {

  @Input() value = '7D';
  @Output() valueChange = new EventEmitter<string>();

  protected readonly ranges = ['Custom', '24H', '7D', '1M', '3M'];

  protected select(range: string): void {
    this.valueChange.emit(range);
  }

  /** Returns the full class list for a segment so hover only applies to inactive items. */
  protected buttonClass(range: string): string {
    const active = this.value === range;
    return [
      'px-3 py-1.5 text-xs font-medium rounded-md transition-colors cursor-pointer',
      active ? 'bg-blue-500 text-white' : 'text-slate-500 hover:bg-slate-100',
    ].join(' ');
  }

}
