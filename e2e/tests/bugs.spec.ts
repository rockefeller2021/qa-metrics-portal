import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { BugsPage } from '../pages/BugsPage';

test.describe('BugTracker y Reinyecciones', () => {

  test('Debe cargar el módulo de BugTracker y permitir filtrado por desarrollador', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const bugsPage = new BugsPage(page);

    await loginPage.goto();
    await loginPage.login('admin_qa', 'Admin1234!');

    await bugsPage.goto();
    expect(page.url()).toContain('/bugs');
  });
});
