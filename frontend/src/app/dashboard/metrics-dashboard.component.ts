import { Component, signal, computed, inject, OnInit, ElementRef, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AuthService } from '../auth/services/auth.service';
import { environment } from '../../environments/environment';
import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { PeriodFilterComponent, PeriodSelection } from '../shared/period-filter.component';
import { NavbarComponent } from '../shared/navbar.component';

import * as echarts from 'echarts';

export interface DashboardMetrics {
  qualityPercentage: number;
  executionRatio: number;
  targetAchieved: boolean;
  alertLevel: 'GREEN' | 'YELLOW' | 'RED';
  totalCases: number;
  successfulCases: number;
  bugsFound: number;
  reinjections: number;
  executionsPerSuccess?: number;
}

interface DashboardData {
  fabrica: DashboardMetrics;
  minorDemand: DashboardMetrics;
  consolidated: DashboardMetrics;
  qualityTarget: number;
}

export interface MonthlyTrendData {
  monthName: string;
  year: number;
  month: number;
  fabricaQuality: number;
  minorDemandQuality: number;
  consolidatedQuality: number;
  successfulCases: number;
  bugsFound: number;
  targetQuality: number;
}

@Component({
  selector: 'app-metrics-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ThemeToggleComponent, PeriodFilterComponent, NavbarComponent],
  templateUrl: './metrics-dashboard.component.html',
  styleUrl: './metrics-dashboard.component.css'
})
export class MetricsDashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  private http = inject(HttpClient);
  authService  = inject(AuthService);

  @ViewChild('chartTrendContainer') chartTrendContainer!: ElementRef;
  private trendChartInstance: echarts.ECharts | null = null;

  // ── Signals ───────────────────────────────────────────────
  selectedYear  = signal<number | ''>('');
  selectedMonth = signal<number | ''>('');

  dashboardData = signal<DashboardData | null>(null);
  trendData     = signal<MonthlyTrendData[]>([]);
  isLoading     = signal(true);
  error         = signal('');
  activeTab     = signal<'consolidated' | 'fabrica' | 'minorDemand'>('consolidated');

  // ── Computed ──────────────────────────────────────────────
  currentMetrics = computed(() => {
    const data = this.dashboardData();
    if (!data) return null;
    return data[this.activeTab()];
  });

  qualityMetric = computed(() => this.currentMetrics()?.qualityPercentage ?? 0);
  isTargetAchieved = computed(() => this.qualityMetric() >= 95);

  gaugeColor = computed(() => {
    const q = this.qualityMetric();
    if (q >= 95) return '#10b981';
    if (q >= 90) return '#f59e0b';
    return '#ef4444';
  });

  gaugeDashOffset = computed(() => {
    const circumference = 2 * Math.PI * 54;
    const offset = circumference - (this.qualityMetric() / 100) * circumference;
    return offset;
  });

  ngOnInit(): void {
    this.loadDashboard();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initTrendChart(), 350);
  }

  ngOnDestroy(): void {
    if (this.trendChartInstance) {
      this.trendChartInstance.dispose();
    }
  }

  onPeriodChange(selection: PeriodSelection): void {
    this.selectedYear.set(selection.year);
    this.selectedMonth.set(selection.month);
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading.set(true);
    let params = new HttpParams();
    if (this.selectedYear()) {
      params = params.set('year', this.selectedYear().toString());
    }
    if (this.selectedMonth()) {
      params = params.set('month', this.selectedMonth().toString());
    }

    this.http.get<DashboardData>(`${environment.apiUrl}/metrics/dashboard`, { params })
      .subscribe({
        next: (data) => {
          this.dashboardData.set(data);
          this.isLoading.set(false);
          setTimeout(() => this.ensureChartRendered(), 100);
        },
        error: () => {
          this.dashboardData.set(this.getEmptyData());
          this.isLoading.set(false);
        }
      });

    // Cargar tendencia mensual
    let trendParams = new HttpParams();
    if (this.selectedYear()) {
      trendParams = trendParams.set('year', this.selectedYear().toString());
    }

    this.http.get<MonthlyTrendData[]>(`${environment.apiUrl}/metrics/trend`, { params: trendParams })
      .subscribe({
        next: (data) => {
          this.trendData.set(data);
          setTimeout(() => this.ensureChartRendered(), 100);
        },
        error: () => this.trendData.set([])
      });
  }

  setTab(tab: 'consolidated' | 'fabrica' | 'minorDemand'): void {
    this.activeTab.set(tab);
  }

  private initTrendChart(): void {
    this.ensureChartRendered();
  }

  private ensureChartRendered(): void {
    if (!this.chartTrendContainer?.nativeElement) return;
    if (!this.trendChartInstance) {
      this.trendChartInstance = echarts.init(this.chartTrendContainer.nativeElement);
      window.addEventListener('resize', () => this.trendChartInstance?.resize());
    }
    this.updateTrendChart();
  }

  private updateTrendChart(): void {
    if (!this.trendChartInstance) {
      if (this.chartTrendContainer?.nativeElement) {
        this.trendChartInstance = echarts.init(this.chartTrendContainer.nativeElement);
        window.addEventListener('resize', () => this.trendChartInstance?.resize());
      } else {
        return;
      }
    }
    const data = this.trendData();
    const months = data.map(d => d.monthName);

    const option: echarts.EChartsOption = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          let res = `<div class="font-bold text-sm mb-1 text-slate-100">${params[0].axisValue}</div>`;
          params.forEach((item: any) => {
            res += `<div class="flex items-center justify-between gap-4 text-xs py-0.5">
                      <span style="color:${item.color}">● ${item.seriesName}:</span>
                      <span class="font-mono font-bold text-white">${item.value}%</span>
                    </div>`;
          });
          return res;
        }
      },
      legend: {
        data: ['🏭 Fábrica', '🔧 Minor Demand', '🌐 Consolidado', 'Target 95%'],
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
      series: [
        {
          name: '🏭 Fábrica',
          type: 'bar',
          data: data.map(d => d.fabricaQuality),
          itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] },
          label: { show: true, position: 'top', formatter: (params: any) => params.value != null ? `Fábrica: ${params.value}%` : '', color: '#6366f1', fontWeight: 'bold', fontSize: 10, textBorderColor: '#ffffff', textBorderWidth: 2 }
        },
        {
          name: '🔧 Minor Demand',
          type: 'bar',
          data: data.map(d => d.minorDemandQuality),
          itemStyle: { color: '#0891b2', borderRadius: [4, 4, 0, 0] },
          label: { show: true, position: 'top', formatter: (params: any) => params.value != null ? `Minor Demand: ${params.value}%` : '', color: '#0891b2', fontWeight: 'bold', fontSize: 10, textBorderColor: '#ffffff', textBorderWidth: 2 }
        },
        {
          name: '🌐 Consolidado',
          type: 'line',
          data: data.map(d => d.consolidatedQuality),
          smooth: true,
          symbolSize: 8,
          itemStyle: { color: '#10b981' },
          lineStyle: { width: 3 }
        },
        {
          name: 'Target 95%',
          type: 'line',
          data: months.map(() => 95),
          lineStyle: { color: '#f43f5e', type: 'dashed', width: 2.5 },
          symbol: 'none'
        }
      ]
    };

    this.trendChartInstance.setOption(option, true);
  }

  private getEmptyData(): DashboardData {
    const empty: DashboardMetrics = {
      qualityPercentage: 100,
      executionRatio: 1,
      targetAchieved: true,
      alertLevel: 'GREEN',
      totalCases: 0,
      successfulCases: 0,
      bugsFound: 0,
      reinjections: 0,
      executionsPerSuccess: 0
    };
    return {
      qualityTarget: 95,
      consolidated: { ...empty },
      fabrica: { ...empty },
      minorDemand: { ...empty }
    };
  }

  formatPercent(value: number): string {
    return value.toFixed(1) + '%';
  }
}
