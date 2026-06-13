export type RequestStatus = 'success' | 'error' | 'pending';

export type SortColumn =
  | 'createdAt'
  | 'status'
  | 'request'
  | 'response'
  | 'model'
  | 'totalTokens'
  | 'promptTokens'
  | 'completionTokens';

export type SortDirection = 'asc' | 'desc';

export interface RequestRow {
  id: string;
  /** ISO timestamp — rendered via Angular's DatePipe. */
  createdAt: string;
  status: RequestStatus;
  request: string;
  response: string;
  model: string;
  totalTokens: number;
  promptTokens: number;
  completionTokens: number;
}

export interface Filters {
  status?: RequestStatus;
  model?: string;
}
