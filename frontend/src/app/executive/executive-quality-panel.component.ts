import { Component, signal, computed, inject, OnInit, ElementRef, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';
import { environment } from '../../environments/environment';
import { ThemeToggleComponent } from '../theme/theme-toggle.component';
import { PeriodFilterComponent, PeriodSelection } from '../shared/period-filter.component';
import { NavbarComponent } from '../shared/navbar.component';

import * as echarts from 'echarts';

export interface ExecutiveMetrics {
  qualityTargetScore: number;
  isTargetMet: boolean;
  executionRatioPct: number;
  executionEffort: number;
  totalDesignedCases: number;
  totalSuccessfulCases: number;
  totalFailedCases: number;
  totalBlockedCases: number;
  totalExecutions: number;
  totalBugs: number;
  reinjectionsCount: number;
  openBugsCount: number;
  resolvedBugsCount: number;
  reinjectionRatePct: number;
  totalDeliveries: number;
  onTimeSla: number;
  delayedSla: number;
  slaComplianceRatio: number;
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
  selector: 'app-executive-quality-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ThemeToggleComponent, PeriodFilterComponent, NavbarComponent],
  templateUrl: './executive-quality-panel.component.html',
  styleUrl: './executive-quality-panel.component.css'
})
export class ExecutiveQualityPanelComponent implements OnInit, AfterViewInit, OnDestroy {

  private http = inject(HttpClient);
  authService  = inject(AuthService);

  @ViewChild('chartExecutiveTrendContainer') chartExecutiveTrendContainer!: ElementRef;
  private trendChartInstance: echarts.ECharts | null = null;

  selectedProjectType = signal<string>('');
  selectedYear        = signal<number | ''>('');
  selectedMonth       = signal<number | ''>('');

  metrics             = signal<ExecutiveMetrics | null>(null);
  trendData           = signal<MonthlyTrendData[]>([]);
  isLoading           = signal<boolean>(false);

  ngOnInit(): void {
    this.loadMetrics();
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
    this.loadMetrics();
  }

  loadMetrics(): void {
    this.isLoading.set(true);
    let params = new HttpParams();
    if (this.selectedProjectType()) {
      params = params.set('projectType', this.selectedProjectType());
    }
    if (this.selectedYear()) {
      params = params.set('year', this.selectedYear().toString());
    }
    if (this.selectedMonth()) {
      params = params.set('month', this.selectedMonth().toString());
    }

    this.http.get<ExecutiveMetrics>(`${environment.apiUrl}/metrics/executive`, { params }).subscribe({
      next: (data) => {
        this.metrics.set(data);
        this.isLoading.set(false);
        setTimeout(() => this.ensureChartRendered(), 100);
      },
      error: () => this.isLoading.set(false)
    });

    let trendParams = new HttpParams();
    if (this.selectedProjectType()) {
      trendParams = trendParams.set('projectType', this.selectedProjectType());
    }
    if (this.selectedYear()) {
      trendParams = trendParams.set('year', this.selectedYear().toString());
    }

    this.http.get<MonthlyTrendData[]>(`${environment.apiUrl}/metrics/trend`, { params: trendParams }).subscribe({
      next: (data) => {
        this.trendData.set(data);
        setTimeout(() => this.ensureChartRendered(), 100);
      },
      error: () => this.trendData.set([])
    });
  }

  filterByType(type: string): void {
    this.selectedProjectType.set(type);
    this.loadMetrics();
  }

  private initTrendChart(): void {
    this.ensureChartRendered();
  }

  private ensureChartRendered(): void {
    if (!this.chartExecutiveTrendContainer?.nativeElement) return;
    if (!this.trendChartInstance) {
      this.trendChartInstance = echarts.init(this.chartExecutiveTrendContainer.nativeElement);
      window.addEventListener('resize', () => this.trendChartInstance?.resize());
    }
    this.updateTrendChart();
  }

  private updateTrendChart(): void {
    if (!this.trendChartInstance) {
      if (this.chartExecutiveTrendContainer?.nativeElement) {
        this.trendChartInstance = echarts.init(this.chartExecutiveTrendContainer.nativeElement);
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
            const val = item.seriesType === 'line' ? `${item.value}%` : item.value;
            res += `<div class="flex items-center justify-between gap-4 text-xs py-0.5">
                      <span style="color:${item.color}">● ${item.seriesName}:</span>
                      <span class="font-mono font-bold text-white">${val}</span>
                    </div>`;
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
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}%', color: '#64748b', fontWeight: 'bold' },
        splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } }
      },
      series: [
        {
          name: '% Calidad Consolidada',
          type: 'bar',
          data: data.map(d => d.consolidatedQuality),
          itemStyle: {
            color: (params: any) => params.value >= 95 ? '#10b981' : '#f43f5e',
            borderRadius: [6, 6, 0, 0]
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}%',
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

    this.trendChartInstance.setOption(option, true);
  }
}
