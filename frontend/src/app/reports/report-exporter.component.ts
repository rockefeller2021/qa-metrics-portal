import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReportService } from './services/report.service';
import { AuthService } from '../auth/services/auth.service';

import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { PeriodFilterComponent, PeriodSelection } from '../shared/period-filter.component';
import { NavbarComponent } from '../shared/navbar.component';

@Component({
  selector: 'app-report-exporter',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ThemeToggleComponent, PeriodFilterComponent, NavbarComponent],
  templateUrl: './report-exporter.component.html',
  styleUrl: './report-exporter.component.css'
})
export class ReportExporterComponent {

  reportService = inject(ReportService);
  authService   = inject(AuthService);

  selectedProjectType = signal<string>('');
  selectedYear        = signal<number | ''>('');
  selectedMonth       = signal<number | ''>('');
  selectedDeveloper   = signal<string>('');
  selectedAnalyst     = signal<string>('');
  isDownloadingPdf   = signal<boolean>(false);
  isDownloadingExcel = signal<boolean>(false);
  isDownloadingPptx  = signal<boolean>(false);

  onPeriodChange(selection: PeriodSelection): void {
    this.selectedYear.set(selection.year);
    this.selectedMonth.set(selection.month);
  }

  exportPdf(): void {
    this.isDownloadingPdf.set(true);
    this.reportService.downloadPdf(
      this.selectedProjectType(),
      this.selectedYear(),
      this.selectedMonth(),
      this.selectedDeveloper(),
      this.selectedAnalyst()
    ).subscribe({
      next: (blob) => {
        this.reportService.saveBlob(blob, `QA_Informe_Ejecutivo_${this.getDateStr()}.pdf`);
        this.isDownloadingPdf.set(false);
      },
      error: () => this.isDownloadingPdf.set(false)
    });
  }

  exportExcel(): void {
    this.isDownloadingExcel.set(true);
    this.reportService.downloadExcel(
      this.selectedProjectType(),
      this.selectedYear(),
      this.selectedMonth(),
      this.selectedDeveloper(),
      this.selectedAnalyst()
    ).subscribe({
      next: (blob) => {
        this.reportService.saveBlob(blob, `QA_Consolidado_Metricas_${this.getDateStr()}.xlsx`);
        this.isDownloadingExcel.set(false);
      },
      error: () => this.isDownloadingExcel.set(false)
    });
  }

  exportPptx(): void {
    this.isDownloadingPptx.set(true);
    this.reportService.downloadPptx(
      this.selectedProjectType(),
      this.selectedYear(),
      this.selectedMonth(),
      this.selectedDeveloper(),
      this.selectedAnalyst()
    ).subscribe({
      next: (blob) => {
        this.reportService.saveBlob(blob, `QA_Presentacion_Comite_${this.getDateStr()}.pptx`);
        this.isDownloadingPptx.set(false);
      },
      error: () => this.isDownloadingPptx.set(false)
    });
  }

  private getDateStr(): string {
    const d = new Date();
    return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
  }
}
