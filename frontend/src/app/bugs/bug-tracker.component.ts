import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BugService } from './services/bug.service';
import { AuthService } from '../auth/services/auth.service';
import { AlertService } from '../shared/services/alert.service';
import { Bug, BugStatus, DefectType } from './models/bug.models';
import { ProjectType } from '../executions/models/execution.models';

import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { BulkImportModalComponent } from '../import/bulk-import-modal.component';
import { MailConfigModalComponent } from '../mail/mail-config-modal.component';
import { PeriodFilterComponent, PeriodSelection } from '../shared/period-filter.component';
import { NavbarComponent } from '../shared/navbar.component';

@Component({
  selector: 'app-bug-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ThemeToggleComponent, BulkImportModalComponent, MailConfigModalComponent, PeriodFilterComponent, NavbarComponent],
  templateUrl: './bug-tracker.component.html',
  styleUrl: './bug-tracker.component.css'
})
export class BugTrackerComponent implements OnInit {

  bugService   = inject(BugService);
  authService  = inject(AuthService);
  alertService = inject(AlertService);

  // ── Signals de Filtro y UI ──────────────────────────────────
  selectedProjectType  = signal<ProjectType | ''>('');
  selectedYear         = signal<number | ''>('');
  selectedMonth        = signal<number | ''>('');
  selectedStatus       = signal<BugStatus | ''>('');
  selectedDeveloper    = signal<string>('');
  onlyReinjections     = signal<boolean>(false);
  searchQuery          = signal<string>('');
  showCreateModal      = signal<boolean>(false);
  showImportModal      = signal<boolean>(false);
  showMailModal        = signal<boolean>(false);

  // ── Bulk Delete Signals ─────────────────────────────────────
  selectedIds = signal<Set<number>>(new Set());

  // Formulario Nuevo Bug
  newBugJiraId     = signal<string>('');
  newRequirementId = signal<string>('');
  newProjectType   = signal<ProjectType>('FABRICA');
  newSprintOrPi    = signal<string>('Sprint 1');
  newStatus        = signal<BugStatus>('OPEN');
  newDefectType    = signal<DefectType>('FUNCTIONAL');
  newReportedDate  = signal<string>(new Date().toISOString().substring(0, 10));
  newReportedBy    = signal<string>('');
  newDeveloperName = signal<string>('');
  newDescription   = signal<string>('');

  // Lista única de desarrolladores asignados para el filtro dropdown
  uniqueDevelopers = computed(() => {
    const list = this.bugService.bugs();
    const set = new Set<string>();
    list.forEach(b => {
      if (b.developerName && b.developerName.trim()) {
        set.add(b.developerName.trim());
      }
    });
    return Array.from(set).sort();
  });

  // ── Computed Counters & Lists ──────────────────────────────
  filteredBugs = computed(() => {
    const list   = this.bugService.bugs();
    const query  = this.searchQuery().toLowerCase().trim();
    const pType  = this.selectedProjectType();
    const status = this.selectedStatus();
    const dev    = this.selectedDeveloper();
    const reinj  = this.onlyReinjections();

    return list.filter(item => {
      const matchType  = !pType || item.projectType === pType;
      const matchStatus= !status || item.status === status;
      const matchDev   = !dev || item.developerName === dev;
      const matchReinj = !reinj || item.reinjectionFlag;
      const matchQuery = !query ||
        item.bugJiraId.toLowerCase().includes(query) ||
        item.requirementId.toLowerCase().includes(query) ||
        (item.reportedBy && item.reportedBy.toLowerCase().includes(query)) ||
        (item.developerName && item.developerName.toLowerCase().includes(query)) ||
        (item.description && item.description.toLowerCase().includes(query));
      return matchType && matchStatus && matchDev && matchReinj && matchQuery;
    });
  });

  hasSelection = computed(() => this.selectedIds().size > 0);
  selectionCount = computed(() => this.selectedIds().size);
  allSelected = computed(() => {
    const filtered = this.filteredBugs();
    return filtered.length > 0 && filtered.every(b => b.id && this.selectedIds().has(b.id));
  });

  totalBugsCount    = computed(() => this.bugService.bugs().length);
  openBugsCount     = computed(() => this.bugService.bugs().filter(b => b.status === 'OPEN' || b.status === 'IN_PROGRESS' || b.status === 'REOPENED').length);
  resolvedBugsCount = computed(() => this.bugService.bugs().filter(b => b.status === 'RESOLVED' || b.status === 'CLOSED').length);
  reinjectionsCount = computed(() => this.bugService.bugs().filter(b => b.reinjectionFlag).length);

  ngOnInit(): void {
    this.loadBugs();
  }

  onPeriodChange(selection: PeriodSelection): void {
    this.selectedYear.set(selection.year);
    this.selectedMonth.set(selection.month);
    this.loadBugs();
  }

  loadBugs(): void {
    this.bugService.getBugs(
      this.selectedProjectType(),
      undefined,
      this.selectedYear(),
      this.selectedMonth()
    ).subscribe();
  }

  filterByType(type: ProjectType | ''): void {
    this.selectedProjectType.set(type);
    this.loadBugs();
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
      const ids = new Set(this.filteredBugs().map(b => b.id!).filter(id => id != null));
      this.selectedIds.set(ids);
    }
  }

  clearSelection(): void {
    this.selectedIds.set(new Set());
  }

  // ── Modales y Formulario ──────────────────────────────────
  openCreateModal(): void {
    this.resetForm();
    this.newReportedBy.set(this.authService.currentUser()?.username || 'Analista QA');
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
    this.resetForm();
  }

  createBug(): void {
    if (!this.newBugJiraId() || !this.newRequirementId()) return;

    this.bugService.createBug({
      bugJiraId:       this.newBugJiraId().trim().toUpperCase(),
      requirementId:   this.newRequirementId().trim().toUpperCase(),
      projectType:     this.newProjectType(),
      sprintOrPi:      this.newSprintOrPi().trim(),
      status:          this.newStatus(),
      defectType:      this.newDefectType(),
      reportedDate:    this.newReportedDate(),
      reportedBy:      this.newReportedBy().trim(),
      developerName:   this.newDeveloperName().trim(),
      description:     this.newDescription().trim(),
      reinjectionFlag: false
    }).subscribe({
      next: () => {
        this.closeCreateModal();
        this.alertService.success('Bug registrado', 'La incidencia fue creada correctamente.');
      },
      error: () => this.alertService.error('Error al crear bug', 'Revisa los datos e inténtalo de nuevo.')
    });
  }

  changeStatus(bug: Bug, newStatus: BugStatus, event: Event): void {
    event.stopPropagation();
    if (!bug.id) return;
    this.bugService.updateBugStatus(bug.id, newStatus).subscribe({
      next: () => this.alertService.success('Estado actualizado'),
      error: () => this.alertService.error('Error al actualizar estado')
    });
  }

  async deleteBug(id: number, event: Event): Promise<void> {
    event.stopPropagation();
    const confirmed = await this.alertService.confirmDelete(
      '¿Eliminar incidencia?',
      'Se eliminará permanentemente del BugTracker.'
    );
    if (confirmed) {
      this.bugService.deleteBug(id).subscribe({
        next: () => this.alertService.success('Bug eliminado'),
        error: () => this.alertService.error('Error al eliminar el bug')
      });
    }
  }

  async deleteSelected(): Promise<void> {
    const ids = Array.from(this.selectedIds());
    const confirmed = await this.alertService.confirm(
      `¿Eliminar ${ids.length} bug${ids.length > 1 ? 's' : ''}?`,
      'Los registros seleccionados se eliminarán permanentemente.',
      'Sí, eliminar seleccionados'
    );
    if (confirmed) {
      this.bugService.deleteBulk(false, ids).subscribe({
        next: () => {
          this.clearSelection();
          this.alertService.success(`${ids.length} bug${ids.length > 1 ? 's' : ''} eliminado${ids.length > 1 ? 's' : ''}`);
        },
        error: () => this.alertService.error('Error al eliminar los bugs seleccionados')
      });
    }
  }

  async deleteAll(): Promise<void> {
    const confirmed = await this.alertService.confirmDanger(
      '¿Eliminar TODOS los bugs?',
      `Se eliminarán los ${this.bugService.bugs().length} bugs registrados. Esta acción no se puede deshacer.`
    );
    if (confirmed) {
      this.bugService.deleteBulk(true).subscribe({
        next: () => {
          this.clearSelection();
          this.alertService.success('Todos los bugs eliminados');
        },
        error: () => this.alertService.error('Error al eliminar todos los bugs')
      });
    }
  }

  private resetForm(): void {
    this.newBugJiraId.set('');
    this.newRequirementId.set('');
    this.newReportedBy.set('');
    this.newDeveloperName.set('');
    this.newDescription.set('');
    this.newDefectType.set('FUNCTIONAL');
  }
}
