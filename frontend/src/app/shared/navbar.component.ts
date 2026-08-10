import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';
import { ThemeToggleComponent } from '../theme/theme-toggle.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

  authService = inject(AuthService);

  showQaMenu      = signal<boolean>(false);
  showMetricsMenu = signal<boolean>(false);
  showMobileMenu  = signal<boolean>(false);

  toggleQaMenu(): void {
    this.showQaMenu.set(!this.showQaMenu());
    if (this.showQaMenu()) this.showMetricsMenu.set(false);
  }

  toggleMetricsMenu(): void {
    this.showMetricsMenu.set(!this.showMetricsMenu());
    if (this.showMetricsMenu()) this.showQaMenu.set(false);
  }

  closeMenus(): void {
    this.showQaMenu.set(false);
    this.showMetricsMenu.set(false);
    this.showMobileMenu.set(false);
  }
}
