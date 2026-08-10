import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ClientDeliveryMetric, ClientReturn, ClientTrackingSummary } from '../models/client-tracking.models';
import { ProjectType } from '../../executions/models/execution.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClientTrackingService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/client-tracking`;

  metrics   = signal<ClientDeliveryMetric[]>([]);
  returns   = signal<ClientReturn[]>([]);
  summary   = signal<ClientTrackingSummary | null>(null);
  isLoading = signal<boolean>(false);
  error     = signal<string>('');

  getMetrics(projectType?: ProjectType | '', year?: number | '', month?: number | ''): Observable<ClientDeliveryMetric[]> {
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    if (year)        params = params.set('year', year.toString());
    if (month)       params = params.set('month', month.toString());

    return this.http.get<ClientDeliveryMetric[]>(`${this.apiUrl}/metrics`, { params }).pipe(
      tap(data => this.metrics.set(data))
    );
  }

  createMetric(metric: ClientDeliveryMetric): Observable<ClientDeliveryMetric> {
    return this.http.post<ClientDeliveryMetric>(`${this.apiUrl}/metrics`, metric);
  }

  deleteMetric(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/metrics/${id}`);
  }

  getReturns(projectType?: ProjectType | '', year?: number | '', month?: number | ''): Observable<ClientReturn[]> {
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    if (year)        params = params.set('year', year.toString());
    if (month)       params = params.set('month', month.toString());

    return this.http.get<ClientReturn[]>(`${this.apiUrl}/returns`, { params }).pipe(
      tap(data => this.returns.set(data))
    );
  }

  createReturn(clientReturn: ClientReturn): Observable<ClientReturn> {
    return this.http.post<ClientReturn>(`${this.apiUrl}/returns`, clientReturn);
  }

  deleteReturn(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/returns/${id}`);
  }

  getSummary(projectType?: ProjectType | '', year?: number | '', month?: number | ''): Observable<ClientTrackingSummary> {
    this.isLoading.set(true);
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    if (year)        params = params.set('year', year.toString());
    if (month)       params = params.set('month', month.toString());

    return this.http.get<ClientTrackingSummary>(`${this.apiUrl}/summary`, { params }).pipe(
      tap({
        next: (data) => {
          this.summary.set(data);
          this.isLoading.set(false);
        },
        error: () => {
          this.error.set('Error al obtener métricas de seguimiento cliente');
          this.isLoading.set(false);
        }
      })
    );
  }
}
