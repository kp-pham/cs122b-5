import { mergeTests } from '@playwright/test';
import { pagesTest } from './pages.fixture';
import { servicesTest } from './services.fixture';

export const test = mergeTests(pagesTest, servicesTest);
export { expect } from '@playwright/test'; 