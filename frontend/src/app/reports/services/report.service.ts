import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Servicio Angular para descarga de archivos binarios (Blob): PDF, XLSX y PPTX.
 */
@Injectable({ providedIn: 'root' })
export class ReportService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reports`;

  downloadPdf(projectType?: string, year?: number | '', month?: number | '', developerName?: string, designerAnalyst?: string): Observable<Blob> {
    let params = new HttpParams();
    if (projectType)     params = params.set('projectType', projectType);
    if (year)            params = params.set('year', year.toString());
    if (month)           params = params.set('month', month.toString());
    if (developerName)   params = params.set('developerName', developerName);
    if (designerAnalyst) params = params.set('designerAnalyst', designerAnalyst);
    return this.http.get(`${this.apiUrl}/pdf`, { params, responseType: 'blob' });
  }

  downloadExcel(projectType?: string, year?: number | '', month?: number | '', developerName?: string, designerAnalyst?: string): Observable<Blob> {
    let params = new HttpParams();
    if (projectType)     params = params.set('projectType', projectType);
    if (year)            params = params.set('year', year.toString());
    if (month)           params = params.set('month', month.toString());
    if (developerName)   params = params.set('developerName', developerName);
    if (designerAnalyst) params = params.set('designerAnalyst', designerAnalyst);
    return this.http.get(`${this.apiUrl}/excel`, { params, responseType: 'blob' });
  }

  downloadPptx(projectType?: string, year?: number | '', month?: number | '', developerName?: string, designerAnalyst?: string): Observable<Blob> {
    let params = new HttpParams();
    if (projectType)     params = params.set('projectType', projectType);
    if (year)            params = params.set('year', year.toString());
    if (month)           params = params.set('month', month.toString());
    if (developerName)   params = params.set('developerName', developerName);
    if (designerAnalyst) params = params.set('designerAnalyst', designerAnalyst);
    return this.http.get(`${this.apiUrl}/pptx`, { params, responseType: 'blob' });
  }

  saveBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
