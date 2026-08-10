import { Page } from '@playwright/test';

/**
 * Page Object Model — Página de Login
 */
export class LoginPage {
  readonly url = '/login';

  constructor(private page: Page) {}

  async goto() {
    await this.page.goto(this.url);
  }

  async login(username: string, password: string) {
    await this.page.fill('#username', username);
    await this.page.fill('#password', password);
    await this.page.click('#btn-login');
  }

  async getErrorMessage(): Promise<string> {
    return await this.page.locator('.error-alert').innerText();
  }

  async isLoginButtonEnabled(): Promise<boolean> {
    return !(await this.page.locator('#btn-login').isDisabled());
  }

  async waitForNavigation() {
    await this.page.waitForURL('**/dashboard');
  }
}
