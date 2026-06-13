import { Injectable, signal } from '@angular/core';


/**
 * Shared UI state for the app shell. Currently holds the sidebar collapse state so
 * the sidebar and the main content area (which adjusts its margin-left) stay in sync
 * without prop-drilling across the shell.
 */
@Injectable({ providedIn: 'root' })
export class LayoutStateService {

  readonly collapsed = signal(false);

  toggle(): void {
    this.collapsed.update((value) => !value);
  }

}
