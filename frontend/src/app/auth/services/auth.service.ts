import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, AuthUser } from '../models/auth.models';
import { environment } from '../../../environments/environment';

/**
 * Servicio de autenticación con Angular Signals.
 * Gestiona el estado del usuario y el token JWT en localStorage.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'qa_portal_token';
  private readonly USER_KEY  = 'qa_portal_user';

  // ── Signals de estado reactivo ───────────────────────────
  private _currentUser = signal<AuthUser | null>(this.loadUserFromStorage());
  private _isLoading   = signal<boolean>(false);

  readonly currentUser   = this._currentUser.asReadonly();
  readonly isLoading     = this._isLoading.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);
  readonly isAdmin         = computed(() => this._currentUser()?.role === 'ROLE_ADMIN');
  readonly isAnalyst       = computed(() => this._currentUser()?.role === 'ROLE_ANALYST');

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    this._isLoading.set(true);
    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/auth/login`, credentials
    ).pipe(
      tap({
        next: (response) => {
          localStorage.setItem(this.TOKEN_KEY, response.token);
          const user = this.parseTokenToUser(response.token);
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
          this._currentUser.set(user);
          this._isLoading.set(false);
        },
        error: () => {
          this._isLoading.set(false);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  private parseTokenToUser(token: string): AuthUser {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      username: payload.sub,
      role: payload.role,
      email: payload.email
    };
  }

  private loadUserFromStorage(): AuthUser | null {
    try {
      const raw = localStorage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
