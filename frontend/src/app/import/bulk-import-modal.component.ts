import { Component, signal, output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-bulk-import-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bulk-import-modal.component.html',
  styleUrl: './bulk-import-modal.component.css'
})
export class BulkImportModalComponent {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/import`;

  close = output<void>();
  imported = output<void>();

  importType     = signal<'executions' | 'bugs' | 'deliveries'>('executions');
  selectedFile   = signal<File | null>(null);
  isUploading    = signal<boolean>(false);
  importResult   = signal<{ importedCount: number; errors: string[] } | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile.set(input.files[0]);
    }
  }

  downloadTemplate(): void {
    const type = this.importType();
    this.http.get(`${this.apiUrl}/template/${type}`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Plantilla_Muestra_${type}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
      }
    });
  }

  uploadFile(): void {
    const file = this.selectedFile();
    if (!file) return;

    this.isUploading.set(true);
    this.importResult.set(null);

    const formData = new FormData();
    formData.append('file', file);

    const endpoint = `${this.apiUrl}/${this.importType()}`;
    this.http.post<{ importedCount: number; errors: string[] }>(endpoint, formData).subscribe({
      next: (res) => {
        this.isUploading.set(false);
        this.importResult.set(res);
        if (res.importedCount > 0) {
          this.imported.emit();
        }
      },
      error: (err) => {
        this.isUploading.set(false);
        this.importResult.set({ importedCount: 0, errors: [err.message || 'Error de importación'] });
      }
    });
  }
}
