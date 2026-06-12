import { test, expect } from '@playwright/test';

const SITE_URL = `https://localhost:8443/cs122b_war/login.html`;

test.describe('customer login', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto(SITE_URL);
    });

    test('page loaded', async ({ page }) => {
        await expect(page).toHaveTitle(/Login/);
        await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
        await expect(page.getByRole('textbox', { name: 'Email Address' })).toBeVisible();
        await expect(page.getByRole('textbox', { name: 'Password' })).toBeVisible();
        await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
    });

    test('incorrect password', async ({ page }) => {
        await login(page, "a@email.com", "a1");
        await expect(page.getByText('Incorrect username or password')).toBeVisible();
    });

    test('successful', async ({ page }) => {
        await login(page, "a@email.com", "a2");
        await expect(page).toHaveURL(/index\.html$/);
        await expect(page.getByRole('button', { name: 'Logout' })).toBeVisible();
    });
});

async function login(page, email, password) {
    await page.getByRole('textbox', { name: 'Email Address' }).fill(email);
    await page.getByRole('textbox', { name: 'Password' }).fill(password);
    await page.getByRole('button', { name: 'Login' }).click();
}