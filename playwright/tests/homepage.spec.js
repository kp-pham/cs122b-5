import { test, expect } from './fixtures';

test.describe("homepage", () => {
    test('redirects to login page upon logout', async ({ loginPage, homePage }) => {
        await homePage.logout();
        await expect(loginPage.signInHeader).toBeVisible();
        await expect(loginPage.emailInput).toBeVisible();
        await expect(loginPage.passwordInput).toBeVisible();
        await expect(loginPage.loginButton).toBeVisible();
    });
});