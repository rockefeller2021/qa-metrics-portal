import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { DeliverySla, DeliverySummary, SlaStatus } from '../models/delivery.models';
import { ProjectType } from '../../executions/models/execution.models';
import { environment } from '../../../environments/environment';

/**
 * Servicio Angular reactivo con Signals para el Seguimiento de Entregas y SLA.
 */
@Injectable({ providedIn: 'root' })
export class DeliveryService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/deliveries`;

  // Signals de estado reactivo
  deliveries = signal<DeliverySla[]>([]);
  summary    = signal<DeliverySummary | null>(null);
  isLoading  = signal<boolean>(false);

  getDeliveries(projectType?: ProjectType | '', status?: SlaStatus | '', sprintOrPi?: string, year?: number | '', month?: number | ''): Observable<DeliverySla[]> {
    this.isLoading.set(true);
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    if (status)      params = params.set('status', status);
    if (sprintOrPi)  params = params.set('sprintOrPi', sprintOrPi);
    if (year)        params = params.set('year', year.toString());
    if (month)       params = params.set('month', month.toString());

    return this.http.get<DeliverySla[]>(this.apiUrl, { params }).pipe(
      tap({
        next: (data) => {
          this.deliveries.set(data);
          this.isLoading.set(false);
          this.getSummary(projectType).subscribe();
        },
        error: () => this.isLoading.set(false)
      })
    );
  }

  getSummary(projectType?: ProjectType | ''): Observable<DeliverySummary> {
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    return this.http.get<DeliverySummary>(`${this.apiUrl}/summary`, { params }).pipe(
      tap((res) => this.summary.set(res))
    );
  }

  createDelivery(delivery: DeliverySla): Observable<DeliverySla> {
    return this.http.post<DeliverySla>(this.apiUrl, delivery).pipe(
      tap(() => this.getDeliveries().subscribe())
    );
  }

  updateDelivery(id: number, delivery: DeliverySla): Observable<DeliverySla> {
    return this.http.put<DeliverySla>(`${this.apiUrl}/${id}`, delivery).pipe(
      tap(() => this.getDeliveries().subscribe())
    );
  }

  deleteDelivery(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.getDeliveries().subscribe())
    );
  }

  deleteBulk(all: boolean, ids?: number[]): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/batch`, {
      body: { all, ids: ids ?? [] }
    }).pipe(
      tap(() => this.getDeliveries().subscribe())
    );
  }
}
