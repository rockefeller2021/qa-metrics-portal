import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { TestExecution, ExecutionRun, ProjectType } from '../models/execution.models';
import { environment } from '../../../environments/environment';

/**
 * Servicio Angular con Signals para la gestión de Ejecuciones de Prueba y Retests.
 */
@Injectable({ providedIn: 'root' })
export class ExecutionService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/executions`;

  // Signals de estado reactivo
  executions = signal<TestExecution[]>([]);
  isLoading  = signal<boolean>(false);
  error      = signal<string>('');

  getExecutions(projectType?: ProjectType | '', sprintOrPi?: string, year?: number | '', month?: number | ''): Observable<TestExecution[]> {
    this.isLoading.set(true);
    let params = new HttpParams();
    if (projectType) params = params.set('projectType', projectType);
    if (sprintOrPi)  params = params.set('sprintOrPi', sprintOrPi);
    if (year)        params = params.set('year', year.toString());
    if (month)       params = params.set('month', month.toString());

    return this.http.get<TestExecution[]>(this.apiUrl, { params }).pipe(
      tap({
        next: (data) => {
          this.executions.set(data);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.error.set('Error al cargar las ejecuciones de prueba');
          this.isLoading.set(false);
        }
      })
    );
  }

  getExecutionById(id: number): Observable<TestExecution> {
    return this.http.get<TestExecution>(`${this.apiUrl}/${id}`);
  }

  createExecution(execution: TestExecution): Observable<TestExecution> {
    return this.http.post<TestExecution>(this.apiUrl, execution).pipe(
      tap(() => this.getExecutions().subscribe())
    );
  }

  updateExecution(id: number, execution: TestExecution): Observable<TestExecution> {
    return this.http.put<TestExecution>(`${this.apiUrl}/${id}`, execution).pipe(
      tap(() => this.getExecutions().subscribe())
    );
  }

  deleteExecution(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.getExecutions().subscribe())
    );
  }

  deleteBulk(all: boolean, ids?: number[]): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/batch`, {
      body: { all, ids: ids ?? [] }
    }).pipe(
      tap(() => this.getExecutions().subscribe())
    );
  }

  getRuns(executionId: number): Observable<ExecutionRun[]> {
    return this.http.get<ExecutionRun[]>(`${this.apiUrl}/${executionId}/runs`);
  }

  addRun(executionId: number, run: Partial<ExecutionRun>): Observable<ExecutionRun> {
    return this.http.post<ExecutionRun>(`${this.apiUrl}/${executionId}/runs`, run).pipe(
      tap(() => this.getExecutions().subscribe())
    );
  }
}
