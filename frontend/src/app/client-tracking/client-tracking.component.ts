import { Component, signal, computed, inject, OnInit, ElementRef, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ClientTrackingService } from './services/client-tracking.service';
import { AuthService } from '../auth/services/auth.service';
import { AlertService } from '../shared/services/alert.service';
import { ClientDeliveryMetric, ClientReturn, ReturnCategory } from './models/client-tracking.models';
import { ProjectType } from '../executions/models/execution.models';
import { PeriodFilterComponent, PeriodSelection } from '../shared/period-filter.component';
import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { NavbarComponent } from '../shared/navbar.component';

import * as echarts from 'echarts';

@Component({
  selector: 'app-client-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, PeriodFilterComponent, ThemeToggleComponent, NavbarComponent],
  templateUrl: './client-tracking.component.html',
  styleUrl: './client-tracking.component.css'
})
export class ClientTrackingComponent implements OnInit, AfterViewInit, OnDestroy {

  trackingService = inject(ClientTrackingService);
  authService     = inject(AuthService);
  alertService    = inject(AlertService);

  @ViewChild('chartContainer') chartContainer!: ElementRef;
  @ViewChild('chartConsolidatedContainer') chartConsolidatedContainer!: ElementRef;

  private chartInstance: echarts.ECharts | null = null;
  private consolidatedChartInstance: echarts.ECharts | null = null;

  // Signals de Filtro y Estado UI
  selectedProjectType = signal<ProjectType | ''>('');
  selectedYear        = signal<number | ''>('');
  selectedMonth       = signal<number | ''>('');

  showMetricModal      = signal<boolean>(false);
  showReturnModal      = signal<boolean>(false);
  showReturnsListModal = signal<boolean>(false);

  // Formulario Nueva Métrica de Entrega
  newProjectType        = signal<ProjectType>('FABRICA');
  newYear               = signal<number>(new Date().getFullYear());
  newMonth              = signal<number>(new Date().getMonth() + 1);
  newSprintOrPeriod     = signal<string>('Semana 1');
  newDeliveryDate       = signal<string>(new Date().toISOString().substring(0, 10));
  newEvolutivosCount    = signal<number>(0);
  newSoportesCount      = signal<number>(0);
  newStandardChangeCount= signal<number>(0);
  newNotes              = signal<string>('');

  // Formulario Nueva Devolución
  newReturnIbl         = signal<string>('');
  newReturnCategory    = signal<ReturnCategory>('EVOLUTIVO');
  newReturnProjectType = signal<ProjectType>('FABRICA');
  newReturnYear        = signal<number>(new Date().getFullYear());
  newReturnMonth       = signal<number>(new Date().getMonth() + 1);
  newReturnRootCause   = signal<string>('');

  // Filtros de la Tabla Modal de Devoluciones
  listFilterYear     = signal<number | null>(null);
  listFilterMonth    = signal<number | null>(null);
  listFilterCategory = signal<string>('TODAS');

  // Filtros de la Tabla de Registros de Entregas
  deliveryFilterYear  = signal<number | null>(null);
  deliveryFilterMonth = signal<number | null>(null);

  // Computed Properties
  summary = computed(() => this.trackingService.summary());
  metrics = computed(() => this.trackingService.metrics());
  returns = computed(() => this.trackingService.returns());

  filteredMetrics = computed(() => {
    let list = this.metrics();
    const y = this.deliveryFilterYear();
    const m = this.deliveryFilterMonth();

    if (y !== null && y > 0) {
      list = list.filter(item => item.year === y);
    }
    if (m !== null && m > 0) {
      list = list.filter(item => item.month === m);
    }
    return list;
  });

  filteredReturns = computed(() => {
    let list = this.returns();
    const y = this.listFilterYear();
    const m = this.listFilterMonth();
    const cat = this.listFilterCategory();

    if (y !== null && y > 0) {
      list = list.filter(r => r.year === y);
    }
    if (m !== null && m > 0) {
      list = list.filter(r => r.month === m);
    }
    if (cat && cat !== 'TODAS') {
      list = list.filter(r => r.category === cat);
    }
    return list;
  });

  resetListFilters(): void {
    this.listFilterYear.set(null);
    this.listFilterMonth.set(null);
    this.listFilterCategory.set('TODAS');
  }

  resetDeliveryFilters(): void {
    this.deliveryFilterYear.set(null);
    this.deliveryFilterMonth.set(null);
  }

  ngOnInit(): void {
    this.loadData();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initCharts(), 300);
  }

  ngOnDestroy(): void {
    if (this.chartInstance) this.chartInstance.dispose();
    if (this.consolidatedChartInstance) this.consolidatedChartInstance.dispose();
  }

  onPeriodChange(selection: PeriodSelection): void {
    this.selectedYear.set(selection.year);
    this.selectedMonth.set(selection.month);
    this.loadData();
  }

  filterByType(type: ProjectType | ''): void {
    this.selectedProjectType.set(type);
    this.loadData();
  }

  loadData(): void {
    const pType = this.selectedProjectType();
    const year  = this.selectedYear();
    const month = this.selectedMonth();

    this.trackingService.getMetrics(pType, year, month).subscribe();
    this.trackingService.getReturns(pType, year, month).subscribe();
    this.trackingService.getSummary(pType, year, month).subscribe({
      next: () => this.updateCharts()
    });
  }

  // ── Operaciones de Entregas ────────────────────────────────
  openMetricModal(): void {
    this.resetMetricForm();
    this.showMetricModal.set(true);
  }

  closeMetricModal(): void {
    this.showMetricModal.set(false);
  }

  createMetric(): void {
    if (this.newEvolutivosCount() < 0 || this.newSoportesCount() < 0 || this.newStandardChangeCount() < 0) {
      this.alertService.error('Ingreso no válido', 'Las cantidades de entregas no pueden ser negativas.');
      return;
    }

    const payload: ClientDeliveryMetric = {
      projectType:          this.newProjectType(),
      year:                 Number(this.newYear()),
      month:                Number(this.newMonth()),
      sprintOrPeriod:       this.newSprintOrPeriod().trim(),
      deliveryDate:         this.newDeliveryDate(),
      evolutivosCount:      Number(this.newEvolutivosCount()),
      soportesCount:        Number(this.newSoportesCount()),
      standardChangeCount:  Number(this.newStandardChangeCount()),
      notes:                this.newNotes().trim()
    };

    this.trackingService.createMetric(payload).subscribe({
      next: () => {
        this.closeMetricModal();
        this.alertService.success('Entregas Registradas', 'Se han guardado correctamente las entregas del cliente.');
        this.loadData();
      },
      error: () => this.alertService.error('Error al guardar', 'Ocurrió un error al registrar las entregas.')
    });
  }

  async deleteMetric(id: number, event: Event): Promise<void> {
    event.stopPropagation();
    const confirmed = await this.alertService.confirmDelete('¿Eliminar registro de entregas?', 'Esta acción no se puede deshacer.');
    if (confirmed) {
      this.trackingService.deleteMetric(id).subscribe({
        next: () => {
          this.alertService.success('Eliminado');
          this.loadData();
        },
        error: () => this.alertService.error('Error al eliminar')
      });
    }
  }

  // ── Operaciones de Devoluciones IBL ───────────────────────
  openReturnModal(): void {
    this.resetReturnForm();
    this.showReturnModal.set(true);
  }

  closeReturnModal(): void {
    this.showReturnModal.set(false);
  }

  createReturn(): void {
    if (!this.newReturnIbl().trim() || !this.newReturnRootCause().trim()) {
      this.alertService.error('Campos obligatorios', 'Por favor ingresa el código IBL y la Causa Raíz.');
      return;
    }

    const payload: ClientReturn = {
      ibl:         this.newReturnIbl().trim().toUpperCase(),
      category:    this.newReturnCategory(),
      projectType: this.newReturnProjectType(),
      year:        this.newReturnYear(),
      month:       this.newReturnMonth(),
      rootCause:   this.newReturnRootCause().trim()
    };

    this.trackingService.createReturn(payload).subscribe({
      next: (res) => {
        this.closeReturnModal();
        if (res.countedInQuality) {
          this.alertService.warning('Devolución Reincidente', `El requerimiento ${res.ibl} es devuelto por ${res.returnCount}ª vez. SÍ se contabiliza como defecto en la calidad.`);
        } else {
          this.alertService.info('Devolución por 1ª vez', `El requerimiento ${res.ibl} es devuelto por 1ª vez. Queda registrado pero NO resta en el porcentaje de calidad.`);
        }
        this.loadData();
      },
      error: () => this.alertService.error('Error al registrar devolución', 'Revisa el código IBL e inténtalo de nuevo.')
    });
  }

  async deleteReturn(id: number): Promise<void> {
    const confirmed = await this.alertService.confirmDelete('¿Eliminar devolución del cliente?', 'Se eliminará del historial.');
    if (confirmed) {
      this.trackingService.deleteReturn(id).subscribe({
        next: () => {
          this.alertService.success('Devolución eliminada');
          this.loadData();
        },
        error: () => this.alertService.error('Error al eliminar devolución')
      });
    }
  }

  // ── Gráficas ECharts Bar Charts ───────────────────────────
  private initCharts(): void {
    if (this.chartContainer) {
      this.chartInstance = echarts.init(this.chartContainer.nativeElement);
    }
    if (this.chartConsolidatedContainer) {
      this.consolidatedChartInstance = echarts.init(this.chartConsolidatedContainer.nativeElement);
    }
    this.updateCharts();
    window.addEventListener('resize', () => {
      this.chartInstance?.resize();
      this.consolidatedChartInstance?.resize();
    });
  }

  private updateCharts(): void {
    this.updateCategoryChart();
    this.updateConsolidatedChart();
  }

  private updateCategoryChart(): void {
    if (!this.chartInstance) return;
    const sum = this.summary();
    if (!sum || !sum.monthlyTrend) return;

    const trend = sum.monthlyTrend;
    const months = trend.map(t => t.monthName);

    const hasEvolutivos = trend.some(t => t.evolutivosQuality != null);
    const hasSoportes = trend.some(t => t.soportesQuality != null);
    const hasStandardChange = trend.some(t => t.standardChangeQuality != null);

    const legendData: string[] = [];
    const seriesList: any[] = [];

    if (hasEvolutivos) {
      legendData.push('Evolutivos');
      seriesList.push({
        name: 'Evolutivos',
        type: 'bar',
        data: trend.map(t => t.evolutivosQuality),
        itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => params.value != null ? `Evolutivos: ${params.value}%` : '',
          color: '#6366f1',
          fontWeight: 'bold',
          fontSize: 10,
          textBorderColor: '#ffffff',
          textBorderWidth: 2
        }
      });
    }

    if (hasSoportes) {
      legendData.push('Soportes');
      seriesList.push({
        name: 'Soportes',
        type: 'bar',
        data: trend.map(t => t.soportesQuality),
        itemStyle: { color: '#0284c7', borderRadius: [4, 4, 0, 0] },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => params.value != null ? `Soportes: ${params.value}%` : '',
          color: '#0284c7',
          fontWeight: 'bold',
          fontSize: 10,
          textBorderColor: '#ffffff',
          textBorderWidth: 2
        }
      });
    }

    if (hasStandardChange) {
      legendData.push('Standard Change');
      seriesList.push({
        name: 'Standard Change',
        type: 'bar',
        data: trend.map(t => t.standardChangeQuality),
        itemStyle: { color: '#9333ea', borderRadius: [4, 4, 0, 0] },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => params.value != null ? `Std Change: ${params.value}%` : '',
          color: '#9333ea',
          fontWeight: 'bold',
          fontSize: 10,
          textBorderColor: '#ffffff',
          textBorderWidth: 2
        }
      });
    }

    legendData.push('Consolidado Calidad', 'Target 95%');

    seriesList.push({
      name: 'Consolidado Calidad',
      type: 'line',
      data: trend.map(t => t.consolidatedQuality),
      smooth: true,
      symbolSize: 8,
      itemStyle: { color: '#10b981' },
      lineStyle: { width: 3 }
    });

    seriesList.push({
      name: 'Target 95%',
      type: 'line',
      data: months.map(() => 95),
      lineStyle: { color: '#f43f5e', type: 'dashed', width: 2 },
      symbol: 'none'
    });

    const option: echarts.EChartsOption = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          let res = `<div class="font-bold text-sm mb-1 text-slate-100">${params[0].axisValue}</div>`;
          params.forEach((item: any) => {
            if (item.value != null) {
              res += `<div class="flex items-center justify-between gap-4 text-xs py-0.5">
                        <span style="color:${item.color}">● ${item.seriesName}:</span>
                        <span class="font-mono font-bold text-white">${item.value}%</span>
                      </div>`;
            }
          });
          return res;
        }
      },
      legend: {
        data: legendData,
        textStyle: { color: '#64748b', fontWeight: 'bold' },
        top: 0
      },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: months.length > 0 ? months : ['Sin datos'],
        axisLine: { lineStyle: { color: '#94a3b8' } },
        axisLabel: { color: '#64748b', fontWeight: 'bold' }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}%', color: '#64748b', fontWeight: 'bold' },
        splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } }
      },
      series: seriesList
    };

    this.chartInstance.setOption(option, true);
  }

  private updateConsolidatedChart(): void {
    if (!this.consolidatedChartInstance) return;
    const sum = this.summary();
    if (!sum || !sum.monthlyTrend) return;

    const trend = sum.monthlyTrend;
    const months = trend.map(t => t.monthName);
    const qualityData = trend.map(t => t.consolidatedQuality);

    const option: echarts.EChartsOption = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          let res = `<div class="font-bold text-sm mb-1 text-slate-100">${params[0].axisValue}</div>`;
          params.forEach((item: any) => {
            if (item.value != null) {
              const val = item.seriesType === 'line' ? `${item.value}%` : `${item.value}%`;
              res += `<div class="flex items-center justify-between gap-4 text-xs py-0.5">
                        <span style="color:${item.color}">● ${item.seriesName}:</span>
                        <span class="font-mono font-bold text-white">${val}</span>
                      </div>`;
            }
          });
          return res;
        }
      },
      legend: {
        data: ['% Calidad Consolidada', 'Target 95%'],
        textStyle: { color: '#64748b', fontWeight: 'bold' },
        top: 0
      },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: months.length > 0 ? months : ['Sin datos'],
        axisLine: { lineStyle: { color: '#94a3b8' } },
        axisLabel: { color: '#64748b', fontWeight: 'bold' }
      },
      yAxis: [
        {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: { formatter: '{value}%', color: '#64748b', fontWeight: 'bold' },
          splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } }
        }
      ],
      series: [
        {
          name: '% Calidad Consolidada',
          type: 'bar',
          data: qualityData,
          itemStyle: {
            color: (params: any) => (params.value != null && params.value >= 95) ? '#10b981' : (params.value != null ? '#f43f5e' : 'transparent'),
            borderRadius: [6, 6, 0, 0]
          },
          label: {
            show: true,
            position: 'top',
            formatter: (params: any) => params.value != null ? `${params.value}%` : '',
            color: '#10b981',
            fontWeight: 'bold',
            fontSize: 11,
            textBorderColor: '#ffffff',
            textBorderWidth: 2
          }
        },
        {
          name: 'Target 95%',
          type: 'line',
          data: months.map(() => 95),
          lineStyle: { color: '#fbbf24', type: 'dashed', width: 2.5 },
          symbol: 'none'
        }
      ]
    };

    this.consolidatedChartInstance.setOption(option, true);
  }

  private resetMetricForm(): void {
    this.newProjectType.set('FABRICA');
    this.newYear.set(new Date().getFullYear());
    this.newMonth.set(new Date().getMonth() + 1);
    this.newSprintOrPeriod.set('Semana 1');
    this.newDeliveryDate.set(new Date().toISOString().substring(0, 10));
    this.newEvolutivosCount.set(0);
    this.newSoportesCount.set(0);
    this.newStandardChangeCount.set(0);
    this.newNotes.set('');
  }

  private resetReturnForm(): void {
    this.newReturnIbl.set('');
    this.newReturnCategory.set('EVOLUTIVO');
    this.newReturnProjectType.set(this.selectedProjectType() || 'FABRICA');
    this.newReturnYear.set(this.selectedYear() || new Date().getFullYear());
    this.newReturnMonth.set(this.selectedMonth() || (new Date().getMonth() + 1));
    this.newReturnRootCause.set('');
  }
}
