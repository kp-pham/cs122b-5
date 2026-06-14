import { test, expect } from './fixtures/base.fixture';

test.use({ storageState: { cookies: [], origins: [] } });

test.describe('customer login', () => {
    test('error message for incorrect message', async ({ loginPage }) => {
        await loginPage.goto();
        await expect(loginPage.signInHeader()).toBeVisible();
        await expect(loginPage.emailInput()).toBeVisible();
        await expect(loginPage.passwordInput()).toBeVisible();
        await expect(loginPage.loginButton()).toBeVisible();

        await loginPage.login("a@email.com", "a1");
        await expect(loginPage.page.getByText('Incorrect username or password')).toBeVisible();
    });

    test('login and loout lifecycle', async ({ loginPage, homePage }) => {
        // Redirect to homepage when correct email and password provided
        await loginPage.goto();
        await loginPage.login("a@email.com", "a2");
        await expect(homePage.logoutButton()).toBeVisible();

        // Redirect to login page when logging out
        await homePage.logout();
        await expect(loginPage.signInHeader()).toBeVisible();
        await expect(loginPage.emailInput()).toBeVisible();
        await expect(loginPage.passwordInput()).toBeVisible();
        await expect(loginPage.loginButton()).toBeVisible();
    });
});