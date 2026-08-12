import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from './services/user.service';
import { AuthService } from '../auth/services/auth.service';
import { UserAccount, UserRole } from './models/user.models';

import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { NavbarComponent } from '../shared/navbar.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ThemeToggleComponent, NavbarComponent],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {

  userService = inject(UserService);
  authService = inject(AuthService);

  // Signals de Filtros y Modales
  selectedRole    = signal<UserRole | ''>('');
  searchQuery     = signal<string>('');
  showCreateModal = signal<boolean>(false);
  showPasswordModal = signal<boolean>(false);
  selectedUserForPassword = signal<UserAccount | null>(null);

  // Formulario Crear Usuario
  newUsername = signal<string>('');
  newEmail    = signal<string>('');
  newPassword = signal<string>('');
  newRole     = signal<UserRole>('ROLE_ANALYST');

  // Formulario Reset Password
  resetPasswordValue = signal<string>('');

  // ── Computed Lists & Summaries ────────────────────────────
  filteredUsers = computed(() => {
    const list  = this.userService.users() || [];
    const query = this.searchQuery().toLowerCase().trim();
    const role  = this.selectedRole();

    return list.filter(u => {
      if (!u) return false;
      const uRole = u.role && (u.role === 'ADMIN' || u.role === 'ROLE_ADMIN') ? 'ROLE_ADMIN' : 'ROLE_ANALYST';
      const matchRole  = !role || uRole === role;
      const username   = u.username ? u.username.toLowerCase() : '';
      const email      = u.email ? u.email.toLowerCase() : '';
      const matchQuery = !query || username.includes(query) || email.includes(query);
      return matchRole && matchQuery;
    });
  });

  totalUsers   = computed(() => (this.userService.users() || []).length);
  adminUsers   = computed(() => (this.userService.users() || []).filter(u => u && (u.role === 'ROLE_ADMIN' || u.role === 'ADMIN')).length);
  analystUsers = computed(() => (this.userService.users() || []).filter(u => u && (u.role === 'ROLE_ANALYST' || u.role === 'ANALYST')).length);
  activeUsers  = computed(() => (this.userService.users() || []).filter(u => u && u.active).length);

  ngOnInit(): void {
    this.userService.getUsers().subscribe({
      error: (err) => console.error('Error al cargar lista de usuarios:', err)
    });
  }

  openCreateModal(): void {
    this.resetCreateForm();
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
    this.resetCreateForm();
  }

  createUser(): void {
    if (!this.newUsername() || !this.newPassword()) return;

    this.userService.createUser({
      username: this.newUsername().trim().toLowerCase(),
      email:    this.newEmail() ? this.newEmail().trim() : undefined,
      password: this.newPassword().trim(),
      role:     this.newRole()
    }).subscribe({
      next: () => {
        alert('Usuario creado exitosamente.');
        this.closeCreateModal();
      },
      error: (err) => {
        const msg = err?.error?.message || err?.message || 'No se pudo crear el usuario.';
        alert('Error al crear usuario: ' + msg);
      }
    });
  }

  toggleStatus(user: UserAccount): void {
    if (!user.id) return;
    this.userService.toggleUserStatus(user.id, !user.active).subscribe({
      error: (err) => alert('Error al cambiar estado del usuario: ' + (err?.error?.message || err?.message))
    });
  }

  openPasswordModal(user: UserAccount): void {
    this.selectedUserForPassword.set(user);
    this.resetPasswordValue.set('');
    this.showPasswordModal.set(true);
  }

  closePasswordModal(): void {
    this.showPasswordModal.set(false);
    this.selectedUserForPassword.set(null);
    this.resetPasswordValue.set('');
  }

  updatePassword(): void {
    const user = this.selectedUserForPassword();
    if (!user || !user.id || !this.resetPasswordValue()) return;

    this.userService.updatePassword(user.id, this.resetPasswordValue().trim()).subscribe({
      next: () => {
        alert('Contraseña actualizada correctamente.');
        this.closePasswordModal();
      },
      error: (err) => alert('Error al actualizar contraseña: ' + (err?.error?.message || err?.message))
    });
  }

  deleteUser(id: number, username: string): void {
    if (confirm(`¿Está seguro de eliminar al usuario '${username}' del sistema?`)) {
      this.userService.deleteUser(id).subscribe({
        error: (err) => alert('Error al eliminar usuario: ' + (err?.error?.message || err?.message))
      });
    }
  }

  private resetCreateForm(): void {
    this.newUsername.set('');
    this.newEmail.set('');
    this.newPassword.set('');
    this.newRole.set('ROLE_ANALYST');
  }
}
