import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ExecutionService } from './services/execution.service';
import { AuthService } from '../auth/services/auth.service';
import { AlertService } from '../shared/services/alert.service';
import { TestExecution, ExecutionRun, ProjectType, RunStatus } from './models/execution.models';

import { BulkImportModalComponent } from '../import/bulk-import-modal.component';
import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { PeriodFilterComponent, PeriodSelection } from '../shared/period-filter.component';
import { NavbarComponent } from '../shared/navbar.component';

@Component({
  selector: 'app-execution-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, BulkImportModalComponent, ThemeToggleComponent, PeriodFilterComponent, NavbarComponent],
  templateUrl: './execution-list.component.html',
  styleUrl: './execution-list.component.css'
})
export class ExecutionListComponent implements OnInit {

  executionService = inject(ExecutionService);
  authService      = inject(AuthService);
  alertService     = inject(AlertService);

  // ── Signals de Filtro y UI ──────────────────────────────────
  selectedProjectType = signal<ProjectType | ''>('');
  selectedYear        = signal<number | ''>('');
  selectedMonth       = signal<number | ''>('');
  searchQuery         = signal<string>('');
  showCreateModal     = signal<boolean>(false);
  showImportModal     = signal<boolean>(false);
  showRunsModal       = signal<boolean>(false);
  selectedExecution   = signal<TestExecution | null>(null);
  executionRuns       = signal<ExecutionRun[]>([]);
  isLoadingRuns       = signal<boolean>(false);

  // ── Bulk Delete Signals ─────────────────────────────────────
  selectedIds = signal<Set<number>>(new Set());

  // Formulario Nueva Ejecución
  newJiraId             = signal<string>('');
  newProjectType        = signal<ProjectType>('FABRICA');
  newExecutionDate      = signal<string>(new Date().toISOString().substring(0, 10));
  newDesignerAnalyst    = signal<string>('');
  newSprintOrPi         = signal<string>('Sprint 1');
  newDescription        = signal<string>('');
  newTotalCases         = signal<number>(0);
  newSuccessfulCases    = signal<number>(0);
  newFailedCases        = signal<number>(0);
  newBlockedCases       = signal<number>(0);

  // Formulario Nuevo Retest
  newRunExecutionDate = signal<string>(new Date().toISOString().substring(0, 10));
  newRunAnalyst       = signal<string>('');
  newRunStatus        = signal<RunStatus>('SUCCESSFUL');
  newRunNotes         = signal<string>('');
  newRunCasesExecuted = signal<number>(0);
  newRunCasesPassed   = signal<number>(0);
  newRunCasesFailed   = signal<number>(0);
  newRunCasesBlocked  = signal<number>(0);

  // ── Computed Filtered List ──────────────────────────────────
  filteredExecutions = computed(() => {
    const list   = this.executionService.executions();
    const query  = this.searchQuery().toLowerCase().trim();
    const pType  = this.selectedProjectType();

    return list.filter(item => {
      const matchType  = !pType || item.projectType === pType;
      const matchQuery = !query ||
        item.jiraId.toLowerCase().includes(query) ||
        item.designerAnalyst.toLowerCase().includes(query) ||
        (item.sprintOrPi && item.sprintOrPi.toLowerCase().includes(query));
      return matchType && matchQuery;
    });
  });

  hasSelection   = computed(() => this.selectedIds().size > 0);
  selectionCount = computed(() => this.selectedIds().size);
  allSelected    = computed(() => {
    const filtered = this.filteredExecutions();
    return filtered.length > 0 && filtered.every(e => e.id && this.selectedIds().has(e.id));
  });

  ngOnInit(): void {
    this.loadExecutions();
  }

  onPeriodChange(selection: PeriodSelection): void {
    this.selectedYear.set(selection.year);
    this.selectedMonth.set(selection.month);
    this.loadExecutions();
  }

  loadExecutions(): void {
    this.executionService.getExecutions(
      this.selectedProjectType(),
      undefined,
      this.selectedYear(),
      this.selectedMonth()
    ).subscribe();
  }

  filterByType(type: ProjectType | ''): void {
    this.selectedProjectType.set(type);
    this.loadExecutions();
  }

  // ── Selección múltiple ─────────────────────────────────────
  toggleSelect(id: number): void {
    const current = new Set(this.selectedIds());
    if (current.has(id)) { current.delete(id); } else { current.add(id); }
    this.selectedIds.set(current);
  }

  toggleSelectAll(): void {
    if (this.allSelected()) {
      this.selectedIds.set(new Set());
    } else {
      const ids = new Set(this.filteredExecutions().map(e => e.id!).filter(id => id != null));
      this.selectedIds.set(ids);
    }
  }

  clearSelection(): void {
    this.selectedIds.set(new Set());
  }

  // ── Modales ────────────────────────────────────────────────
  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
    this.resetCreateForm();
  }

  openRunsModal(execution: TestExecution): void {
    this.selectedExecution.set(execution);
    this.showRunsModal.set(true);
    this.loadRuns(execution.id!);
  }

  closeRunsModal(): void {
    this.showRunsModal.set(false);
    this.selectedExecution.set(null);
    this.executionRuns.set([]);
    this.resetRunForm();
  }

  loadRuns(executionId: number): void {
    this.isLoadingRuns.set(true);
    this.executionService.getRuns(executionId).subscribe({
      next: (runs) => {
        this.executionRuns.set(runs);
        this.isLoadingRuns.set(false);
      },
      error: () => this.isLoadingRuns.set(false)
    });
  }

  // ── Acciones CRUD ──────────────────────────────────────────
  createExecution(): void {
    if (!this.newJiraId() || !this.newDesignerAnalyst()) return;

    const execDate = this.newExecutionDate();
    const newExec: TestExecution = {
      jiraId:           this.newJiraId().trim().toUpperCase(),
      projectType:      this.newProjectType(),
      assignmentDate:   execDate,
      designDate:       execDate,
      designerAnalyst:  this.newDesignerAnalyst().trim(),
      sprintOrPi:       this.newSprintOrPi().trim(),
      description:      this.newDescription().trim(),
      totalCases:       Number(this.newTotalCases()) || 0,
      successfulCases:  Number(this.newSuccessfulCases()) || 0,
      failedCases:      Number(this.newFailedCases()) || 0,
      blockedCases:     Number(this.newBlockedCases()) || 0,
    };

    this.executionService.createExecution(newExec).subscribe({
      next: () => {
        this.closeCreateModal();
        this.alertService.success('Ejecución registrada', 'El requerimiento fue creado correctamente.');
      },
      error: () => this.alertService.error('Error al crear ejecución', 'Revisa los datos e inténtalo de nuevo.')
    });
  }

  addRun(): void {
    const exec = this.selectedExecution();
    if (!exec || !exec.id || !this.newRunAnalyst()) return;

    const newRun: Partial<ExecutionRun> = {
      executionDate:     this.newRunExecutionDate(),
      executedByAnalyst: this.newRunAnalyst().trim(),
      status:            this.newRunStatus(),
      notes:             this.newRunNotes().trim(),
      casesExecuted:     Number(this.newRunCasesExecuted()) || 0,
      casesPassed:       Number(this.newRunCasesPassed()) || 0,
      casesFailed:       Number(this.newRunCasesFailed()) || 0,
      casesBlocked:      Number(this.newRunCasesBlocked()) || 0,
    };

    this.executionService.addRun(exec.id, newRun).subscribe({
      next: () => {
        this.loadRuns(exec.id!);
        this.resetRunForm();
        this.alertService.success('Iteración registrada');
      },
      error: () => this.alertService.error('Error al agregar la iteración')
    });
  }

  async deleteExecution(id: number, event: Event): Promise<void> {
    event.stopPropagation();
    const confirmed = await this.alertService.confirmDelete(
      '¿Eliminar ejecución?',
      'Se eliminará la ejecución y todos sus retests asociados.'
    );
    if (confirmed) {
      this.executionService.deleteExecution(id).subscribe({
        next: () => this.alertService.success('Ejecución eliminada'),
        error: () => this.alertService.error('Error al eliminar la ejecución')
      });
    }
  }

  async deleteSelected(): Promise<void> {
    const ids = Array.from(this.selectedIds());
    const confirmed = await this.alertService.confirm(
      `¿Eliminar ${ids.length} ejecución${ids.length > 1 ? 'es' : ''}?`,
      'Las ejecuciones seleccionadas y sus retests serán eliminados permanentemente.',
      'Sí, eliminar seleccionadas'
    );
    if (confirmed) {
      this.executionService.deleteBulk(false, ids).subscribe({
        next: () => {
          this.clearSelection();
          this.alertService.success(`${ids.length} ejecución${ids.length > 1 ? 'es' : ''} eliminada${ids.length > 1 ? 's' : ''}`);
        },
        error: () => this.alertService.error('Error al eliminar las ejecuciones seleccionadas')
      });
    }
  }

  async deleteAll(): Promise<void> {
    const confirmed = await this.alertService.confirmDanger(
      '¿Eliminar TODAS las ejecuciones?',
      `Se eliminarán las ${this.executionService.executions().length} ejecuciones y todos sus retests asociados.`
    );
    if (confirmed) {
      this.executionService.deleteBulk(true).subscribe({
        next: () => {
          this.clearSelection();
          this.alertService.success('Todas las ejecuciones eliminadas');
        },
        error: () => this.alertService.error('Error al eliminar todas las ejecuciones')
      });
    }
  }

  // ── Helpers Matemáticos ──────────────────────────────────
  getTotalExecutions(item: TestExecution): number {
    if (!item.runs || item.runs.length === 0) {
      return item.totalCases || 0;
    }
    const runSum = item.runs.reduce((sum, r) => sum + (r.casesExecuted || 1), 0);
    return Math.max(runSum, item.totalCases || 0);
  }

  getExecutionRatio(item: TestExecution): number {
    const totalExecs = this.getTotalExecutions(item);
    if (totalExecs === 0) return 0;
    const ok = item.successfulCases || 0;
    return Math.min(100, Math.round((ok / totalExecs) * 100));
  }

  getExecutionsPerSuccess(item: TestExecution): string {
    const ok = item.successfulCases || 0;
    if (ok === 0) return 'N/A';
    const totalExecs = this.getTotalExecutions(item);
    const ratio = (totalExecs / ok).toFixed(1);
    return `${ratio} ejecuciones/OK`;
  }

  getRunLabel(runNumber: number): string {
    if (runNumber === 1) return 'Run #1 (Ejecución Inicial)';
    return `Run #${runNumber} (Retest #${runNumber - 1})`;
  }

  isSlaBreached(item: TestExecution): boolean {
    if (!item.commitmentDate || !item.qaDeliveryDate) return false;
    return new Date(item.qaDeliveryDate) > new Date(item.commitmentDate);
  }

  getLatestStatus(item: TestExecution): RunStatus | 'PENDIENTE' {
    if (!item.runs || item.runs.length === 0) return 'PENDIENTE';
    const sorted = [...item.runs].sort((a, b) => b.runNumber - a.runNumber);
    return sorted[0].status;
  }

  private resetCreateForm(): void {
    this.newJiraId.set('');
    this.newDesignerAnalyst.set('');
    this.newExecutionDate.set(new Date().toISOString().substring(0, 10));
    this.newDescription.set('');
    this.newTotalCases.set(0);
    this.newSuccessfulCases.set(0);
    this.newFailedCases.set(0);
    this.newBlockedCases.set(0);
  }

  private resetRunForm(): void {
    this.newRunAnalyst.set('');
    this.newRunNotes.set('');
    this.newRunStatus.set('SUCCESSFUL');
    this.newRunExecutionDate.set(new Date().toISOString().substring(0, 10));
  }
}
