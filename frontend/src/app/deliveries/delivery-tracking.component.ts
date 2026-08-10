import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DeliveryService } from './services/delivery.service';
import { AuthService } from '../auth/services/auth.service';
import { AlertService } from '../shared/services/alert.service';
import { DeliverySla, SlaStatus } from './models/delivery.models';
import { ProjectType } from '../executions/models/execution.models';

import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { BulkImportModalComponent } from '../import/bulk-import-modal.component';
import { MailConfigModalComponent } from '../mail/mail-config-modal.component';
import { PeriodFilterComponent, PeriodSelection } from '../shared/period-filter.component';
import { NavbarComponent } from '../shared/navbar.component';

@Component({
  selector: 'app-delivery-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ThemeToggleComponent, BulkImportModalComponent, MailConfigModalComponent, PeriodFilterComponent, NavbarComponent],
  templateUrl: './delivery-tracking.component.html',
  styleUrl: './delivery-tracking.component.css'
})
export class DeliveryTrackingComponent implements OnInit {

  deliveryService = inject(DeliveryService);
  authService     = inject(AuthService);
  alertService    = inject(AlertService);

  // Signals de UI y Filtros
  selectedProjectType = signal<ProjectType | ''>('');
  selectedYear        = signal<number | ''>('');
  selectedMonth       = signal<number | ''>('');
  selectedStatus      = signal<SlaStatus | ''>('');
  searchQuery         = signal<string>('');
  showModal           = signal<boolean>(false);
  showImportModal     = signal<boolean>(false);
  showMailModal       = signal<boolean>(false);
  editingId           = signal<number | null>(null);

  // ── Bulk Delete Signals ─────────────────────────────────────
  selectedIds = signal<Set<number>>(new Set());

  // Formulario Hito de Entrega
  formJiraId                 = signal<string>('');
  formProjectType            = signal<ProjectType>('FABRICA');
  formSprintOrPi             = signal<string>('Sprint 1');
  formDesignerAnalyst        = signal<string>('');
  formEstimatedDeliveryDate  = signal<string>('');
  formEstimatedQaDate        = signal<string>('');
  formRealQaDate             = signal<string>('');
  formRealClientDeliveryDate = signal<string>('');
  formNotes                  = signal<string>('');

  // ── Computed Lists ────────────────────────────────────────
  filteredDeliveries = computed(() => {
    const list   = this.deliveryService.deliveries();
    const query  = this.searchQuery().toLowerCase().trim();
    const pType  = this.selectedProjectType();
    const status = this.selectedStatus();

    return list.filter(item => {
      const matchType   = !pType || item.projectType === pType;
      const matchStatus = !status || item.status === status;
      const matchQuery  = !query ||
        item.jiraId.toLowerCase().includes(query) ||
        item.designerAnalyst.toLowerCase().includes(query) ||
        item.sprintOrPi.toLowerCase().includes(query);
      return matchType && matchStatus && matchQuery;
    });
  });

  hasSelection   = computed(() => this.selectedIds().size > 0);
  selectionCount = computed(() => this.selectedIds().size);
  allSelected    = computed(() => {
    const filtered = this.filteredDeliveries();
    return filtered.length > 0 && filtered.every(d => d.id && this.selectedIds().has(d.id));
  });

  ngOnInit(): void {
    this.loadDeliveries();
  }

  onPeriodChange(selection: PeriodSelection): void {
    this.selectedYear.set(selection.year);
    this.selectedMonth.set(selection.month);
    this.loadDeliveries();
  }

  loadDeliveries(): void {
    this.deliveryService.getDeliveries(
      this.selectedProjectType(),
      undefined,
      undefined,
      this.selectedYear(),
      this.selectedMonth()
    ).subscribe();
  }

  filterByType(type: ProjectType | ''): void {
    this.selectedProjectType.set(type);
    this.loadDeliveries();
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
      const ids = new Set(this.filteredDeliveries().map(d => d.id!).filter(id => id != null));
      this.selectedIds.set(ids);
    }
  }

  clearSelection(): void {
    this.selectedIds.set(new Set());
  }

  openCreateModal(): void {
    this.editingId.set(null);
    this.resetForm();
    this.showModal.set(true);
  }

  openEditModal(item: DeliverySla): void {
    this.editingId.set(item.id || null);
    this.formJiraId.set(item.jiraId);
    this.formProjectType.set(item.projectType);
    this.formSprintOrPi.set(item.sprintOrPi);
    this.formDesignerAnalyst.set(item.designerAnalyst);
    this.formEstimatedDeliveryDate.set(item.estimatedDeliveryDate || '');
    this.formEstimatedQaDate.set(item.estimatedQaDate || '');
    this.formRealQaDate.set(item.realQaDate || '');
    this.formRealClientDeliveryDate.set(item.realClientDeliveryDate || '');
    this.formNotes.set(item.notes || '');
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
    this.resetForm();
  }

  saveDelivery(): void {
    if (!this.formJiraId() || !this.formDesignerAnalyst() || !this.formEstimatedDeliveryDate()) return;

    const dto: DeliverySla = {
      jiraId:                 this.formJiraId().trim().toUpperCase(),
      projectType:            this.formProjectType(),
      sprintOrPi:             this.formSprintOrPi().trim(),
      designerAnalyst:        this.formDesignerAnalyst().trim(),
      estimatedDeliveryDate:  this.formEstimatedDeliveryDate(),
      estimatedQaDate:        this.formEstimatedQaDate() || undefined,
      realQaDate:             this.formRealQaDate() || undefined,
      realClientDeliveryDate: this.formRealClientDeliveryDate() || undefined,
      status:                 'PENDING',
      delayDays:              0,
      notes:                  this.formNotes().trim()
    };

    if (this.editingId()) {
      this.deliveryService.updateDelivery(this.editingId()!, dto).subscribe({
        next: () => {
          this.closeModal();
          this.alertService.success('Hito actualizado', 'Los datos de la entrega fueron guardados.');
        },
        error: () => this.alertService.error('Error al actualizar el hito SLA')
      });
    } else {
      this.deliveryService.createDelivery(dto).subscribe({
        next: () => {
          this.closeModal();
          this.alertService.success('Hito registrado', 'El seguimiento SLA fue creado correctamente.');
        },
        error: () => this.alertService.error('Error al crear el hito SLA')
      });
    }
  }

  async deleteDelivery(id: number, event: Event): Promise<void> {
    event.stopPropagation();
    const confirmed = await this.alertService.confirmDelete(
      '¿Eliminar hito de entrega?',
      'Se eliminará el registro SLA del requerimiento.'
    );
    if (confirmed) {
      this.deliveryService.deleteDelivery(id).subscribe({
        next: () => this.alertService.success('Hito eliminado'),
        error: () => this.alertService.error('Error al eliminar el hito SLA')
      });
    }
  }

  async deleteSelected(): Promise<void> {
    const ids = Array.from(this.selectedIds());
    const confirmed = await this.alertService.confirm(
      `¿Eliminar ${ids.length} hito${ids.length > 1 ? 's' : ''} SLA?`,
      'Los registros seleccionados serán eliminados permanentemente.',
      'Sí, eliminar seleccionados'
    );
    if (confirmed) {
      this.deliveryService.deleteBulk(false, ids).subscribe({
        next: () => {
          this.clearSelection();
          this.alertService.success(`${ids.length} hito${ids.length > 1 ? 's' : ''} eliminado${ids.length > 1 ? 's' : ''}`);
        },
        error: () => this.alertService.error('Error al eliminar los hitos seleccionados')
      });
    }
  }

  async deleteAll(): Promise<void> {
    const confirmed = await this.alertService.confirmDanger(
      '¿Eliminar TODOS los hitos SLA?',
      `Se eliminarán los ${this.deliveryService.deliveries().length} registros de seguimiento.`
    );
    if (confirmed) {
      this.deliveryService.deleteBulk(true).subscribe({
        next: () => {
          this.clearSelection();
          this.alertService.success('Todos los hitos SLA eliminados');
        },
        error: () => this.alertService.error('Error al eliminar todos los hitos')
      });
    }
  }

  private resetForm(): void {
    this.formJiraId.set('');
    this.formDesignerAnalyst.set(this.authService.currentUser()?.username || 'Analista QA');
    this.formEstimatedDeliveryDate.set('');
    this.formEstimatedQaDate.set('');
    this.formRealQaDate.set('');
    this.formRealClientDeliveryDate.set('');
    this.formNotes.set('');
  }
}
