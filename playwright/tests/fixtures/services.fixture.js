import { test as base } from '@playwright/test';

import LoginService from '../../services/LoginService';

export const servicesTest = base.extend({
    loginService: async ({ request }, use) => {
        const loginService = new LoginService(request);
        await use(loginService);
    }
});

export { expect } from '@playwright/test';