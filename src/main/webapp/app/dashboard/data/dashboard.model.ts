/** Shared shape for a single colored slice of the Errors donut. */
export interface ErrorsSegment {
  code: string;
  value: number;
  color: string;
}

export interface RequestsMetric {
  labels: string[];
  success: number[];
  error: number[];
  kpi: string;
}

export interface ErrorsMetric {
  segments: ErrorsSegment[];
  total: string;
  totalLabel: string;
}

export interface TopModel {
  name: string;
  count: string;
}

export interface CostsMetric {
  labels: string[];
  values: number[];
  kpi: string;
}

export interface TopCountry {
  flag: string;
  name: string;
  iso: string;
  count: string;
}

export interface LatencyMetric {
  labels: string[];
  values: number[];
  kpi: string;
  kpiUnit: string;
  /** Y-range of the shaded reference band, in the same unit as `values`. */
  band: { from: number; to: number };
}
