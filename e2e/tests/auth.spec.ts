import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

test.describe('Módulo de Autenticación — QA Metrics Portal', () => {

  test('Login exitoso con admin_qa debe redirigir al dashboard', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await expect(page).toHaveTitle(/QA Metrics Portal/);
    await expect(page.locator('h1')).toContainText('QA Metrics Portal');

    await loginPage.login('admin_qa', 'Admin1234!');
    await loginPage.waitForNavigation();

    await expect(page).toHaveURL(/.*dashboard/);
  });

  test('Login con credenciales incorrectas debe mostrar mensaje de error', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await loginPage.login('admin_qa', 'WrongPassword');

    await expect(page.locator('form')).toContainText('Credenciales inválidas');
  });

  test('Botón login deshabilitado con campos vacíos', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    const isEnabled = await loginPage.isLoginButtonEnabled();
    expect(isEnabled).toBeFalsy();
  });

  test('Ruta protegida /dashboard redirige a /login sin sesión', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/.*login/);
  });

  test('Toggle de contraseña cambia el tipo del campo', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    const passwordInput = page.locator('#password');
    await expect(passwordInput).toHaveAttribute('type', 'password');

    await page.click('button[aria-label="Mostrar contraseña"]');
    await expect(passwordInput).toHaveAttribute('type', 'text');

    await page.click('button[aria-label="Mostrar contraseña"]');
    await expect(passwordInput).toHaveAttribute('type', 'password');
  });

  test('Flujo completo: login → dashboard → logout', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login('admin_qa', 'Admin1234!');
    await loginPage.waitForNavigation();

    // Verificar que está en dashboard
    await expect(page.locator('h1')).toContainText('Dashboard de Calidad');

    // Logout
    await page.click('button[title="Cerrar sesión"]');
    await expect(page).toHaveURL(/.*login/);
  });
});
