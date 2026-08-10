import { Page } from '@playwright/test';

/**
 * Page Object Model — Módulo de Entregas y Gobierno SLA
 */
export class DeliveriesPage {
  readonly url = '/deliveries';

  constructor(private page: Page) {}

  async goto() {
    await this.page.goto(this.url);
  }

  async openNewDeliveryModal() {
    await this.page.click('button:has-text("Registrar Hito SLA")');
  }

  async fillDeliveryForm(data: { jiraId: string; designerAnalyst: string; estimatedDeliveryDate: string }) {
    await this.page.fill('input[placeholder*="HUB-100"]', data.jiraId);
    await this.page.fill('input[placeholder*="Nombre del analista"]', data.designerAnalyst);
  }

  async submitDeliveryForm() {
    await this.page.click('button:has-text("Guardar Hito SLA")');
  }
}
