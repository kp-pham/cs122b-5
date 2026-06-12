import { test, expect } from './fixtures';

test.describe('customer login', () => {
    test('page loaded', async ({ loginPage }) => {
        await expect(loginPage.signInHeader).toBeVisible();
        await expect(loginPage.emailInput).toBeVisible();
        await expect(loginPage.passwordInput).toBeVisible();
        await expect(loginPage.loginButton).toBeVisible();
    });

    test('incorrect password', async ({ loginPage }) => {
        await loginPage.login("a@email.com", "a1");
        await expect(loginPage.page.getByText('Incorrect username or password')).toBeVisible();
    });

    test('successful', async ({ loginPage, homePage }) => {
        await loginPage.login("a@email.com", "a2");
        await expect(homePage.logoutButton).toBeVisible();
    });
});