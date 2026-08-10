import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then(m => m.LoginComponent),
    title: 'Iniciar Sesión — QA Metrics Portal'
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./dashboard/metrics-dashboard.component').then(m => m.MetricsDashboardComponent),
    title: 'Dashboard — QA Metrics Portal'
  },
  {
    path: 'executions',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./executions/execution-list.component').then(m => m.ExecutionListComponent),
    title: 'Ejecuciones — QA Metrics Portal'
  },
  {
    path: 'bugs',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./bugs/bug-tracker.component').then(m => m.BugTrackerComponent),
    title: 'BugTracker — QA Metrics Portal'
  },
  {
    path: 'deliveries',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./deliveries/delivery-tracking.component').then(m => m.DeliveryTrackingComponent),
    title: 'Seguimiento Entregas — QA Metrics Portal'
  },
  {
    path: 'client-tracking',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./client-tracking/client-tracking.component').then(m => m.ClientTrackingComponent),
    title: 'Seguimiento Cliente & Target 95% — QA Metrics Portal'
  },
  {
    path: 'users',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./users/user-management.component').then(m => m.UserManagementComponent),
    title: 'Gestión Usuarios & Roles — QA Metrics Portal'
  },
  {
    path: 'reports',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./reports/report-exporter.component').then(m => m.ReportExporterComponent),
    title: 'Reportes Ejecutivos — QA Metrics Portal'
  },
  {
    path: 'executive-quality',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./executive/executive-quality-panel.component').then(m => m.ExecutiveQualityPanelComponent),
    title: 'Calidad Consolidada 95% — QA Metrics Portal'
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
