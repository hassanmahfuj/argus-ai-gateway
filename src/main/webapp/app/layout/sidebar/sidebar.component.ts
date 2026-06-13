import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IconComponent, IconName } from 'app/common/icons/icon.component';
import { LayoutStateService } from '../layout-state.service';


interface NavItem {
  id: string;
  label: string;
  icon: IconName;
  badgeDot?: boolean;
  /** When set, the item navigates to this router path; otherwise it's a no-op mock item. */
  route?: string;
}

interface NavGroup {
  label: string;
  items: NavItem[];
}


@Component({
  selector: 'app-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgTemplateOutlet, IconComponent, RouterLink],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {

  protected readonly state = inject(LayoutStateService);
  private readonly router = inject(Router);

  /** Alias of the shared collapse signal for template use. */
  protected readonly collapsed = this.state.collapsed;

  protected readonly activeItem = signal('dashboard');

  /** Maps router URLs back to nav item ids so active state survives reload / back-forward. */
  private readonly routeToItem: Readonly<Record<string, string>> = {
    '/': 'dashboard',
    '': 'dashboard',
    '/requests': 'requests',
  };

  constructor() {
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((e) => {
        const id = this.routeToItem[e.urlAfterRedirects];
        if (id) {
          this.activeItem.set(id);
        }
      });
  }

  protected readonly openGroups = signal<Set<string>>(new Set(['Segments', 'Improve', 'Monitor']));

  protected readonly topItems: NavItem[] = [
    { id: 'dashboard', label: 'Dashboard', icon: 'home', route: '' },
    { id: 'requests', label: 'Requests', icon: 'grid', badgeDot: true, route: 'requests' },
  ];

  protected readonly groups: NavGroup[] = [
    {
      label: 'Segments',
      items: [
        { id: 'sessions', label: 'Sessions', icon: 'activity', badgeDot: true },
        { id: 'users', label: 'Users', icon: 'users' },
        { id: 'hql', label: 'HQL', icon: 'code' },
      ],
    },
    {
      label: 'Improve',
      items: [
        { id: 'prompts', label: 'Prompts', icon: 'message' },
        { id: 'datasets', label: 'Datasets', icon: 'database' },
        { id: 'playground', label: 'Playground', icon: 'terminal' },
      ],
    },
    {
      label: 'Monitor',
      items: [
        { id: 'rate-limits', label: 'Rate Limits', icon: 'shield' },
        { id: 'alerts', label: 'Alerts', icon: 'bell' },
      ],
    },
  ];

  protected isActive(id: string): boolean {
    return this.activeItem() === id;
  }

  protected selectItem(id: string): void {
    this.activeItem.set(id);
  }

  protected isGroupOpen(label: string): boolean {
    return this.openGroups().has(label);
  }

  protected toggleGroup(label: string): void {
    this.openGroups.update((set) => {
      const next = new Set(set);
      if (next.has(label)) {
        next.delete(label);
      } else {
        next.add(label);
      }
      return next;
    });
  }

}
