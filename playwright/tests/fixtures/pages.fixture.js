import { test as base } from '@playwright/test';

import LoginPage from '../../pages/LoginPage';
import HomePage from '../../pages/HomePage';
import ResultsPage from '../../pages/ResultsPage';

export const pagesTest = base.extend({
    loginPage: async ({ page }, use) => {
        const loginPage = new LoginPage(page);
        await use(loginPage);
    },
    
    homePage: async ({ page }, use) => {
        const homePage = new HomePage(page);
        await use(homePage);
    },

    resultsPage: async ({ page }, use) => {
        const resultsPage = new ResultsPage(page);
        await use(resultsPage);
    }
});

export { expect } from '@playwright/test';