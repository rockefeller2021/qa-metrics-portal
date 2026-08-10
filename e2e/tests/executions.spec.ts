import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { ExecutionsPage } from '../pages/ExecutionsPage';

test.describe('Módulo de Ejecuciones de Prueba', () => {

  test('Debe cargar la lista de ejecuciones correctamente tras autenticación', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const executionsPage = new ExecutionsPage(page);

    await loginPage.goto();
    await loginPage.login('admin_qa', 'Admin1234!');

    await executionsPage.goto();
    expect(page.url()).toContain('/executions');
  });
});
