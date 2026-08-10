export type ProjectType = 'FABRICA' | 'MINOR_DEMAND';
export type RunStatus = 'SUCCESSFUL' | 'FAILED' | 'BLOCKED' | 'RETEST';

export interface ExecutionRun {
  id?: number;
  testExecutionId?: number;
  runNumber: number;
  executionDate: string;
  executedByAnalyst: string;
  status: RunStatus;
  notes?: string;
  casesExecuted?: number;
  casesPassed?: number;
  casesFailed?: number;
  casesBlocked?: number;
}

export interface TestExecution {
  id?: number;
  jiraId: string;
  projectType: ProjectType;
  assignmentDate: string;
  designDate?: string;
  designerAnalyst: string;
  commitmentDate?: string;      // RF04: Fecha Compromiso Estimado
  qaDeliveryDate?: string;       // RF04: Fecha Real Entrega QA
  clientDeliveryDate?: string;   // RF04: Fecha Real Entrega Cliente
  sprintOrPi?: string;
  description?: string;
  totalCases?: number;          // Total casos de prueba diseñados
  successfulCases?: number;     // Casos exitosos acumulados
  failedCases?: number;         // Casos fallidos acumulados
  blockedCases?: number;        // Casos bloqueados acumulados
  runs?: ExecutionRun[];
  createdAt?: string;
  createdBy?: string;
  lastModifiedBy?: string;
  updatedAt?: string;
}
