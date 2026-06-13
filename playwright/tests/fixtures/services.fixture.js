import { test as base } from '@playwright/test';

import LoginService from '../../services/LoginService';
import MoviesService from '../../services/MoviesService';

export const servicesTest = base.extend({
    loginService: async ({ request }, use) => {
        const loginService = new LoginService(request);
        await use(loginService);
    },

    moviesService: async ({ request }, use) => {
        const moviesService = new MoviesService(request);
        await use(moviesService);
    },
});

export { expect } from '@playwright/test';