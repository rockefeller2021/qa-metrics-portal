import { Injectable, signal, effect } from '@angular/core';

export type ThemeMode = 'dark' | 'light';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  private readonly STORAGE_KEY = 'qa_portal_theme';
  
  // Signal de estado reactivo del tema
  currentTheme = signal<ThemeMode>(this.getInitialTheme());

  constructor() {
    // Effect que aplica/remueve las clases en el body/html y persiste en localStorage
    effect(() => {
      const theme = this.currentTheme();
      const body  = document.body;
      const html  = document.documentElement;

      if (theme === 'light') {
        body.classList.add('light-theme');
        body.classList.remove('dark-theme');
        html.classList.remove('dark');
      } else {
        body.classList.add('dark-theme');
        body.classList.remove('light-theme');
        html.classList.add('dark');
      }

      localStorage.setItem(this.STORAGE_KEY, theme);
    });
  }

  toggleTheme(): void {
    this.currentTheme.update(prev => prev === 'dark' ? 'light' : 'dark');
  }

  setTheme(theme: ThemeMode): void {
    this.currentTheme.set(theme);
  }

  isDark(): boolean {
    return this.currentTheme() === 'dark';
  }

  private getInitialTheme(): ThemeMode {
    const saved = localStorage.getItem(this.STORAGE_KEY) as ThemeMode;
    if (saved === 'light' || saved === 'dark') {
      return saved;
    }
    return 'dark'; // Modo Oscuro por defecto
  }
}
