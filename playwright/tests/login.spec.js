import { test, expect } from '@playwright/test';
import LoginPage from '../pages/LoginPage';

test.describe('customer login', () => {
    let loginPage;

    test.beforeEach(async ({ page }) => {
        loginPage = new LoginPage(page);
        await loginPage.goto();
    });

    test('page loaded', async ({ page }) => {
        await expect(loginPage.signInHeader).toBeVisible();
        await expect(loginPage.emailInput).toBeVisible();
        await expect(loginPage.passwordInput).toBeVisible();
        await expect(loginPage.loginButton).toBeVisible();
    });

    test('incorrect password', async ({ page }) => {
        await loginPage.login("a@email.com", "a1");
        await expect(loginPage.page.getByText('Incorrect username or password')).toBeVisible();
    });

    test('successful', async ({ page }) => {
        await loginPage.login("a@email.com", "a2");
        await expect(page).toHaveURL(/index\.html$/);
        await expect(page.getByRole('button', { name: 'Logout' })).toBeVisible();
    });
});