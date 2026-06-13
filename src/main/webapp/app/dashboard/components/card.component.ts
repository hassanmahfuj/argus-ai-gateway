import { ChangeDetectionStrategy, Component } from '@angular/core';


/** Presentational wrapper for the consistent analytics card: white, 12px radius, subtle border, no shadow. */
@Component({
  selector: 'app-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div class="bg-white rounded-xl border border-slate-200 p-5 h-full"><ng-content /></div>`,
})
export class CardComponent {
}
