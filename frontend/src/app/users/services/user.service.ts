import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { UserAccount, UserRole } from '../models/user.models';
import { environment } from '../../../environments/environment';

/**
 * Servicio Angular con Signals para la Gestión de Usuarios y Roles RBAC.
 */
@Injectable({ providedIn: 'root' })
export class UserService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/users`;

  // Signals de estado reactivo
  users     = signal<UserAccount[]>([]);
  isLoading = signal<boolean>(false);

  getUsers(): Observable<UserAccount[]> {
    this.isLoading.set(true);
    return this.http.get<UserAccount[]>(this.apiUrl).pipe(
      tap({
        next: (data) => {
          this.users.set(data || []);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          console.error('Error HTTP al obtener usuarios:', err);
        }
      })
    );
  }

  createUser(user: { username: string; email?: string; password: string; role: UserRole }): Observable<UserAccount> {
    return this.http.post<UserAccount>(this.apiUrl, user).pipe(
      tap(() => this.getUsers().subscribe())
    );
  }

  toggleUserStatus(id: number, active: boolean): Observable<UserAccount> {
    return this.http.put<UserAccount>(`${this.apiUrl}/${id}/status`, { active }).pipe(
      tap(() => this.getUsers().subscribe())
    );
  }

  updatePassword(id: number, password: string): Observable<UserAccount> {
    return this.http.put<UserAccount>(`${this.apiUrl}/${id}/password`, { password });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.getUsers().subscribe())
    );
  }
}
