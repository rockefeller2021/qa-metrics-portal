import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DeliveriesPage } from '../pages/DeliveriesPage';

test.describe('Gobierno SLA y Seguimiento de Entregas', () => {

  test('Debe cargar el módulo de seguimiento de entregas correctamente', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const deliveriesPage = new DeliveriesPage(page);

    await loginPage.goto();
    await loginPage.login('admin_qa', 'Admin1234!');

    await deliveriesPage.goto();
    expect(page.url()).toContain('/deliveries');
  });
});
