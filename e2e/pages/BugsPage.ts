import { Page } from '@playwright/test';

/**
 * Page Object Model — BugTracker y Detección de Reinyecciones
 */
export class BugsPage {
  readonly url = '/bugs';

  constructor(private page: Page) {}

  async goto() {
    await this.page.goto(this.url);
  }

  async openNewBugModal() {
    await this.page.click('button:has-text("Reportar Bug")');
  }

  async fillBugForm(data: { bugJiraId: string; requirementId: string; developerName: string; description: string }) {
    await this.page.fill('input[placeholder*="BUG-101"]', data.bugJiraId);
    await this.page.fill('input[placeholder*="HU-500"]', data.requirementId);
    await this.page.fill('input[placeholder*="Pedro Dev"]', data.developerName);
    await this.page.fill('textarea', data.description);
  }

  async submitBugForm() {
    await this.page.click('button:has-text("Guardar Incidencia")');
  }

  async searchBug(query: string) {
    await this.page.fill('input[placeholder*="Buscar por ID Bug"]', query);
  }

  async filterByDeveloper(developerName: string) {
    await this.page.selectOption('select:has-text("Todos los Desarrolladores")', developerName);
  }
}
