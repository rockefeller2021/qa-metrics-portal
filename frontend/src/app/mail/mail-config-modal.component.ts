import { Component, signal, output, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';

export interface MailConfig {
  enabled: boolean;
  host: string;
  port: number;
  username: string;
  password?: string;
  fromEmail: string;
  recipientEmail: string;
  authRequired: boolean;
  startTlsEnabled: boolean;
}

@Component({
  selector: 'app-mail-config-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mail-config-modal.component.html',
  styleUrl: './mail-config-modal.component.css'
})
export class MailConfigModalComponent implements OnInit {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/mail`;

  close = output<void>();

  config            = signal<MailConfig | null>(null);
  isLoading         = signal<boolean>(false);
  isTesting         = signal<boolean>(false);
  isSaving          = signal<boolean>(false);
  testResult        = signal<{ success: boolean; message: string } | null>(null);
  testRecipient     = signal<string>('');

  ngOnInit(): void {
    this.loadConfig();
  }

  loadConfig(): void {
    this.isLoading.set(true);
    this.http.get<MailConfig>(`${this.apiUrl}/config`).subscribe({
      next: (data) => {
        this.config.set(data);
        if (data.recipientEmail) {
          this.testRecipient.set(data.recipientEmail);
        }
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  saveConfig(): void {
    const cfg = this.config();
    if (!cfg) return;

    this.isSaving.set(true);
    this.http.post<MailConfig>(`${this.apiUrl}/config`, cfg).subscribe({
      next: (res) => {
        this.config.set(res);
        this.isSaving.set(false);
        this.testResult.set({ success: true, message: '✅ Configuración SMTP guardada correctamente.' });
      },
      error: (err) => {
        this.isSaving.set(false);
        this.testResult.set({ success: false, message: '❌ Error guardando configuración: ' + (err.message || '') });
      }
    });
  }

  testConnection(): void {
    this.isTesting.set(true);
    this.testResult.set(null);

    let params = new HttpParams();
    if (this.testRecipient()) {
      params = params.set('recipient', this.testRecipient());
    }

    this.http.post<{ success: boolean; message: string }>(`${this.apiUrl}/test`, {}, { params }).subscribe({
      next: (res) => {
        this.isTesting.set(false);
        this.testResult.set(res);
      },
      error: (err) => {
        this.isTesting.set(false);
        this.testResult.set({ success: false, message: '❌ Error de comunicación con servidor: ' + (err.message || '') });
      }
    });
  }

  applyPreset(type: 'gmail' | 'outlook'): void {
    const current = this.config();
    if (!current) return;

    if (type === 'gmail') {
      this.config.set({
        ...current,
        host: 'smtp.gmail.com',
        port: 587,
        authRequired: true,
        startTlsEnabled: true
      });
    } else if (type === 'outlook') {
      this.config.set({
        ...current,
        host: 'smtp.office365.com',
        port: 587,
        authRequired: true,
        startTlsEnabled: true
      });
    }
  }
}
