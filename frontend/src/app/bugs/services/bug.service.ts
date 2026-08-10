import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Bug, BugStatus } from '../models/bug.models';
import { ProjectType } from '../../executions/models/execution.models';
import { environment } from '../../../environments/environment';

/**
 * Servicio Angular con Signals para BugTracker y detección de reinyecciones.
 */
@Injectable({ providedIn: 'root' })
export class BugService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/bugs`;

  // Signals de estado reactivo
  bugs       = signal<Bug[]>([]);
  isLoading  = signal<boolean>(false);
  error      = signal<string>('');

  getBugs(projectType?: ProjectType | '', sprintOrPi?: string, year?: number | '', month?: number | ''): Observable<Bug[]> {
    this.isLoading.set(true);
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    if (sprintOrPi)  params = params.set('sprintOrPi', sprintOrPi);
    if (year)        params = params.set('year', year.toString());
    if (month)       params = params.set('month', month.toString());

    return this.http.get<Bug[]>(this.apiUrl, { params }).pipe(
      tap({
        next: (data) => {
          this.bugs.set(data);
          this.isLoading.set(false);
        },
        error: () => {
          this.error.set('Error al cargar los bugs');
          this.isLoading.set(false);
        }
      })
    );
  }

  getBugById(id: number): Observable<Bug> {
    return this.http.get<Bug>(`${this.apiUrl}/${id}`);
  }

  createBug(bug: Bug): Observable<Bug> {
    return this.http.post<Bug>(this.apiUrl, bug).pipe(
      tap(() => this.getBugs().subscribe())
    );
  }

  updateBug(id: number, bug: Bug): Observable<Bug> {
    return this.http.put<Bug>(`${this.apiUrl}/${id}`, bug).pipe(
      tap(() => this.getBugs().subscribe())
    );
  }

  updateBugStatus(id: number, status: BugStatus): Observable<Bug> {
    return this.http.get<Bug>(`${this.apiUrl}/${id}`).pipe(
      tap((bug) => {
        const updated = { ...bug, status };
        if (status === 'RESOLVED' || status === 'CLOSED') {
          updated.resolvedDate = new Date().toISOString().substring(0, 10);
        }
        this.updateBug(id, updated).subscribe();
      })
    );
  }

  deleteBug(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.getBugs().subscribe())
    );
  }

  deleteBulk(all: boolean, ids?: number[]): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/batch`, {
      body: { all, ids: ids ?? [] }
    }).pipe(
      tap(() => this.getBugs().subscribe())
    );
  }

  getReinjections(projectType?: ProjectType): Observable<Bug[]> {
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    return this.http.get<Bug[]>(`${this.apiUrl}/reinjections`, { params });
  }
}
