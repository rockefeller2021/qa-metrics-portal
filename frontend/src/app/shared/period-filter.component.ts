import { Component, signal, output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface PeriodSelection {
  year: number | '';
  month: number | '';
}

@Component({
  selector: 'app-period-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex items-center gap-2 bg-white/[0.03] border border-white/10 p-1.5 rounded-xl">
      <select [value]="selectedYear()" (change)="onYearChange($any($event.target).value)" class="px-3 py-1.5 bg-[#181838] border border-white/10 rounded-lg text-xs font-semibold text-white focus:outline-none focus:border-indigo-500 cursor-pointer">
        <option value="">📅 Todos los Años</option>
        <option [value]="2026">2026</option>
        <option [value]="2025">2025</option>
        <option [value]="2024">2024</option>
      </select>

      <select [value]="selectedMonth()" (change)="onMonthChange($any($event.target).value)" class="px-3 py-1.5 bg-[#181838] border border-white/10 rounded-lg text-xs font-semibold text-white focus:outline-none focus:border-indigo-500 cursor-pointer">
        <option value="">🗓️ Todos los Meses</option>
        <option [value]="1">Enero</option>
        <option [value]="2">Febrero</option>
        <option [value]="3">Marzo</option>
        <option [value]="4">Abril</option>
        <option [value]="5">Mayo</option>
        <option [value]="6">Junio</option>
        <option [value]="7">Julio</option>
        <option [value]="8">Agosto</option>
        <option [value]="9">Septiembre</option>
        <option [value]="10">Octubre</option>
        <option [value]="11">Noviembre</option>
        <option [value]="12">Diciembre</option>
      </select>
    </div>
  `
})
export class PeriodFilterComponent implements OnInit {

  selectedYear  = signal<number | ''>('');
  selectedMonth = signal<number | ''>('');

  periodChange = output<PeriodSelection>();

  ngOnInit(): void {
    this.selectedYear.set(2026);
  }

  onYearChange(val: string): void {
    const yr = val ? Number(val) : '';
    this.selectedYear.set(yr);
    this.emitChange();
  }

  onMonthChange(val: string): void {
    const m = val ? Number(val) : '';
    this.selectedMonth.set(m);
    this.emitChange();
  }

  private emitChange(): void {
    this.periodChange.emit({
      year: this.selectedYear(),
      month: this.selectedMonth()
    });
  }
}
