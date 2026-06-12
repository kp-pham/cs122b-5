import { expect, test as setup } from '@playwright/test'

const authFile = 'playwright/.auth/user.json';

setup('authenticate', async ({ request }) => {
    await request.post('https://localhost:8443/cs122b_war/api/customers/login', {
        form: { email: 'a@email.com', password: 'a2' }
    });
    
    await request.storageState({ path: authFile });
});