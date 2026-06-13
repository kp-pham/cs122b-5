import { test, expect } from './fixtures/base.fixture';

test.describe("homepage", () => {
    test('redirects to login page upon logout', async ({ loginPage, homePage }) => {
        await homePage.goto();
        await expect(homePage.page).toHaveURL('https://localhost:8443/cs122b_war/index.html');
        await homePage.logout();
        await expect(loginPage.signInHeader()).toBeVisible();
        await expect(loginPage.emailInput()).toBeVisible();
        await expect(loginPage.passwordInput()).toBeVisible();
        await expect(loginPage.loginButton()).toBeVisible();
    });
});