import { ProjectType } from '../../executions/models/execution.models';

export type SlaStatus = 'PENDING' | 'ON_TIME' | 'DELAYED';

export interface DeliverySla {
  id?: number;
  jiraId: string;
  projectType: ProjectType;
  sprintOrPi: string;
  designerAnalyst: string;
  estimatedDeliveryDate: string; // Fecha estimada entrega cliente
  estimatedQaDate?: string;      // Fecha pruebas QA estimada
  realQaDate?: string;           // Fecha real QA
  realClientDeliveryDate?: string;// Fecha real entrega cliente
  status: SlaStatus;
  delayDays: number;
  notes?: string;
  createdAt?: string;
  createdBy?: string;
  lastModifiedBy?: string;
  updatedAt?: string;
}

export interface DeliverySummary {
  total: number;
  onTime: number;
  delayed: number;
  pending: number;
  slaComplianceRatio: number;
}
