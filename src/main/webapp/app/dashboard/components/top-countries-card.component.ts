import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { TopCountry } from '../data/dashboard.model';


/** Top Countries card: flag + name | ISO pill | count, with a "Show All" button. */
@Component({
  selector: 'app-top-countries-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex flex-col h-full">
      <span class="text-[13px] text-slate-400 mb-3">Top Countries</span>
      <div class="flex flex-col">
        @for (country of countries; track country.iso) {
          <div class="flex items-center gap-2 py-2 border-b border-slate-100 last:border-0">
            <span class="text-base leading-none">{{ country.flag }}</span>
            <span class="text-[13px] text-slate-700 truncate flex-1">{{ country.name }}</span>
            <span class="px-1.5 py-0.5 rounded text-[10px] font-medium bg-slate-100 text-slate-500">{{ country.iso }}</span>
            <span class="text-[13px] text-slate-700 tabular-nums w-20 text-right">{{ country.count }}</span>
          </div>
        }
      </div>
      <button type="button"
              class="mt-3 w-full py-2 text-xs font-medium text-slate-600 border border-slate-200 rounded-lg hover:bg-slate-100 cursor-pointer">
        Show All
      </button>
    </div>
  `,
})
export class TopCountriesCardComponent {

  @Input({ required: true }) countries!: TopCountry[];

}
