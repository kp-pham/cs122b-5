import { test as base } from '@playwright/test';
import fs from 'fs';
import path from 'path';

import LoginPage from '../pages/LoginPage';
import HomePage from '../pages/HomePage';
import LoginService from '../services/LoginService';

export const test = base.extend({
    loginPage: async ({ page }, use) => {
        const loginPage = new LoginPage(page);
        await use(loginPage);
    },
    
    homePage: async ({ page }, use) => {
        const homePage = new HomePage(page);
        await use(homePage);
    },

    loginService: async ({ request }, use) => {
        const loginService = new LoginService(request);
        await use(loginService);
    }
});

export { expect } from '@playwright/test';