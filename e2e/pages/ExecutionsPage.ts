import { Page } from '@playwright/test';

/**
 * Page Object Model — Módulo de Ejecuciones de Prueba
 */
export class ExecutionsPage {
  readonly url = '/executions';

  constructor(private page: Page) {}

  async goto() {
    await this.page.goto(this.url);
  }

  async openNewExecutionModal() {
    await this.page.click('#btn-new-execution');
  }

  async fillExecutionForm(data: { jiraId: string; designerAnalyst: string; totalCases: number; successfulCases: number }) {
    await this.page.fill('input[placeholder*="HUB-1234"]', data.jiraId);
    await this.page.fill('input[placeholder*="Nombre del analista"]', data.designerAnalyst);
  }

  async submitExecutionForm() {
    await this.page.click('button:has-text("Guardar Ejecución")');
  }

  async searchExecution(query: string) {
    await this.page.fill('input[placeholder*="Buscar por Jira ID"]', query);
  }

  async getExecutionCount(): Promise<number> {
    return await this.page.locator('tbody tr').count();
  }
}
