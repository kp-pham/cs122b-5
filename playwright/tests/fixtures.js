import { test as base } from '@playwright/test';
import LoginPage from '../pages/LoginPage';
import HomePage from '../pages/HomePage';
import LoginClient from '../api-clients/LoginClient';

export const test = base.extend({
    loginPage: async ({ page }, use) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await use(loginPage);
    },
    
    homePage: async ({ page }, use) => {
        const homePage = new HomePage(page);
        await use(homePage);
    },

    loginClient: async ({ request }, use) => {
        const loginClient = new LoginClient(request);
        await use(loginClient);
    }
});

export { expect } from '@playwright/test';