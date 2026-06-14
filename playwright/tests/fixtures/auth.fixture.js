import test, { test as baseTest, request } from '@playwright/test';
import fs from 'fs';
import path from 'path';

const accountsPath = path.resolve(__dirname, 'accounts.json');

if (!fs.existsSync(accountsPath)) {
    throw new Exception('accounts.json does not exist');
}

const ACCOUNT_POOL = JSON.parse(fs.readFileSync(accountsPath));

export const authTest = baseTest.extend({
    storageState: ({ workerStorageState }, use) => use(workerStorageState),

    workerStorageState: [
        async ({}, use) => {
            const id = test.info().parallelIndex;
            const fileName = path.resolve(test.info().project.outputDir,  `.auth/${id}.json`);

            if (fs.existsSync(fileName)) {
                await use(fileName);
                return;
            }

            const context = await request.newContext({ storageState: undefined, ignoreHTTPSErrors: true });
            
            const account = acquireAccount(id);

            await context.post('https://localhost:8443/cs122b_war/api/customers/login', {
                form: {
                    email: account.email,
                    password: account.password
                }
            });

            await context.storageState({ path: fileName });
            await context.dispose();
            await use(fileName);
        }, { scope: 'worker' }
    ]
});

function acquireAccount(id) {
    return ACCOUNT_POOL[id % ACCOUNT_POOL.length];
}