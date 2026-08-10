import { ProjectType } from '../../executions/models/execution.models';

export type ReturnCategory = 'EVOLUTIVO' | 'SOPORTE' | 'STANDARD_CHANGE';

export interface ClientReturn {
  id?: number;
  clientDeliveryMetricId?: number;
  projectType?: ProjectType;
  year?: number;
  month?: number;
  ibl: string;
  category: ReturnCategory;
  rootCause: string;
  returnCount?: number;
  countedInQuality?: boolean;
  returnDate?: string;
  createdAt?: string;
  createdBy?: string;
  lastModifiedBy?: string;
  updatedAt?: string;
}

export interface ClientDeliveryMetric {
  id?: number;
  projectType: ProjectType;
  year: number;
  month: number;
  sprintOrPeriod: string;
  deliveryDate: string;
  evolutivosCount: number;
  soportesCount: number;
  standardChangeCount: number;
  notes?: string;
  createdAt?: string;
  createdBy?: string;
  lastModifiedBy?: string;
  updatedAt?: string;
  returns?: ClientReturn[];
}

export interface MonthlyQualityData {
  monthName: string;
  year: number;
  month: number;
  evolutivosQuality: number;
  soportesQuality: number;
  standardChangeQuality: number;
  consolidatedQuality: number;
}

export interface ClientTrackingSummary {
  totalEvolutivos: number;
  defectsEvolutivos: number;
  qualityEvolutivos: number;

  totalSoportes: number;
  defectsSoportes: number;
  qualitySoportes: number;

  totalStandardChange: number;
  defectsStandardChange: number;
  qualityStandardChange: number;

  totalDeliveries: number;
  totalDefects: number;
  consolidatedQuality: number;
  targetQuality: number;

  monthlyTrend: MonthlyQualityData[];
}
