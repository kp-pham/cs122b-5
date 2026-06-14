import { mergeTests } from '@playwright/test';
import { authTest } from './auth.fixture';
import { pagesTest } from './pages.fixture';
import { servicesTest } from './services.fixture';

export const test = mergeTests(authTest, pagesTest, servicesTest);
export { expect } from '@playwright/test'; 