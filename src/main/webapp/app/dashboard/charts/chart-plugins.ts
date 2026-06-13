import { ChartOptions, Plugin } from 'chart.js';


/**
 * Scale config that hides the y-axis + gridlines and shows ONLY the first and last
 * x-axis labels (e.g. "22 Feb" … "23 May"). Used by the Requests, Costs and Latency
 * charts so they read like the reference design.
 *
 * Non-generic: 'line' and 'bar' share identical cartesian scale option types, so the
 * returned object satisfies both `ChartOptions<'line'>['scales']` and the bar variant.
 */
export function endOnlyScales(): NonNullable<ChartOptions<'line'>['scales']> {
  return {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        color: '#94A3B8',
        font: { size: 11 },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        callback(this: unknown, _value: unknown, index: number): string {
          const labels = ((this as any)?.chart?.data?.labels ?? []) as string[];
          return index === 0 || index === labels.length - 1 ? (labels[index] ?? '') : '';
        },
      },
    },
    y: { display: false, grid: { display: false } },
  } as unknown as NonNullable<ChartOptions<'line'>['scales']>;
}

/**
 * Draws two centered lines (a big total + a small label) over a doughnut, mimicking
 * Recharts' center-text overlay. The `read` closure lets the text stay fresh without
 * rebuilding the plugin array.
 */
export function createDonutCenterPlugin(read: () => { total: string; label: string }): Plugin<'doughnut'> {
  return {
    id: 'donutCenterText',
    beforeDraw(chart) {
      const { ctx, chartArea } = chart;
      if (!chartArea) {
        return;
      }
      const { total, label } = read();
      const cx = (chartArea.left + chartArea.right) / 2;
      const cy = (chartArea.top + chartArea.bottom) / 2;

      ctx.save();
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillStyle = '#0F172A';
      ctx.font = '600 22px ui-sans-serif, system-ui, -apple-system, sans-serif';
      ctx.fillText(total, cx, cy - 10);
      ctx.fillStyle = '#94A3B8';
      ctx.font = '500 11px ui-sans-serif, system-ui, -apple-system, sans-serif';
      ctx.fillText(label, cx, cy + 14);
      ctx.restore();
    },
  };
}

/**
 * Paints a filled horizontal band across the plot area between two y-values, behind
 * the datasets (so the latency area fill overlays it). Mimics Recharts' ReferenceArea.
 */
export function createRefBandPlugin(read: () => { from: number; to: number; color: string }): Plugin<'line'> {
  return {
    id: 'latencyRefBand',
    beforeDatasetsDraw(chart) {
      const { ctx, chartArea, scales } = chart;
      const y = scales['y'];
      if (!chartArea || !y) {
        return;
      }
      const { from, to, color } = read();
      const top = y.getPixelForValue(Math.max(from, to));
      const bottom = y.getPixelForValue(Math.min(from, to));

      ctx.save();
      ctx.fillStyle = color;
      ctx.fillRect(chartArea.left, top, chartArea.right - chartArea.left, bottom - top);
      ctx.restore();
    },
  };
}
