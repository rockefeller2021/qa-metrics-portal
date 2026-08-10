import { ProjectType } from '../../executions/models/execution.models';

export type BugStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REOPENED';
export type DefectType = 'FUNCTIONAL' | 'UI_UX' | 'PERFORMANCE' | 'SECURITY' | 'DATA' | 'ENVIRONMENT' | 'REINJECTION';

export interface Bug {
  id?: number;
  bugJiraId: string;
  requirementId: string;
  projectType: ProjectType;
  sprintOrPi: string;
  status: BugStatus;
  defectType: DefectType;
  description: string;
  reinjectionFlag: boolean;
  reportedDate: string;
  resolvedDate?: string;
  reportedBy?: string;
  developerName?: string;
  createdAt?: string;
  createdBy?: string;
  lastModifiedBy?: string;
  updatedAt?: string;
}
