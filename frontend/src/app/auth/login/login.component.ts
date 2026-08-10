import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Componente Standalone de Login con Angular Signals.
 * Diseño glassmorphism con animaciones CSS.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  private authService = inject(AuthService);
  private router      = inject(Router);

  // ── Signals ───────────────────────────────────────────────
  username     = signal('');
  password     = signal('');
  showPassword = signal(false);
  errorMessage = signal('');
  isLoading    = signal(false);

  // ── Computed ──────────────────────────────────────────────
  isFormValid = computed(() =>
    this.username().trim().length >= 3 && this.password().length >= 6
  );

  onLogin(): void {
    if (!this.isFormValid()) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.login({
      username: this.username(),
      password: this.password()
    }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401 || err.status === 403) {
          this.errorMessage.set('Usuario o contraseña incorrectos.');
        } else if (err.status === 423) {
          this.errorMessage.set('Usuario inactivo. Contacte al administrador.');
        } else {
          this.errorMessage.set('Error de conexión. Intente de nuevo.');
        }
      }
    });
  }

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }
}
