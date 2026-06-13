import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { TopModel } from '../data/dashboard.model';


/** Top Models card: two-column table with pastel model-name pills + right-aligned counts. */
@Component({
  selector: 'app-top-models-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex flex-col h-full">
      <span class="text-[13px] text-slate-400 mb-3">Top Models</span>
      <div class="flex flex-col">
        @for (model of models; track model.name; let i = $index) {
          <div class="flex items-center justify-between gap-3 py-2 border-b border-slate-100 last:border-0">
            <span class="px-2 py-0.5 rounded text-xs font-medium" [class]="pillClass(i)">{{ model.name }}</span>
            <span class="text-[13px] text-slate-700 tabular-nums whitespace-nowrap">{{ model.count }}</span>
          </div>
        }
      </div>
    </div>
  `,
})
export class TopModelsCardComponent {

  @Input({ required: true }) models!: TopModel[];

  private readonly palette = [
    'bg-green-100 text-green-700',
    'bg-yellow-100 text-yellow-800',
    'bg-orange-100 text-orange-700',
    'bg-purple-100 text-purple-700',
    'bg-rose-100 text-rose-700',
    'bg-violet-100 text-violet-700',
  ];

  protected pillClass(index: number): string {
    return this.palette[index % this.palette.length];
  }

}
