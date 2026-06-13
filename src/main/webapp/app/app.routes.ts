import { Routes } from '@angular/router';
import { ErrorComponent } from './error/error.component';
import { DashboardComponent } from './dashboard/dashboard.component';


export const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
    title: 'Dashboard'
  },
  {
    path: 'error',
    component: ErrorComponent,
    title: $localize`:@@error.page.headline:Error`
  },
  {
    path: '**',
    component: ErrorComponent,
    title: $localize`:@@notFound.headline:Page not found`
  }
];
