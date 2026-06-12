import { test, expect } from '../fixtures';

test.use({ storageState: { cookies: [], origins: [] } });

test.describe("login service API endpoints", () => {
    test('return 200 and cookie when login successful', async ({ loginService }) => {
        const response = await loginService.login({
            email: "a@email.com",
            password: "a2"
        });

        expect(response.status()).toBe(200);

        const setCookieHeader = response.headers()['set-cookie'];
        
        expect(setCookieHeader).not.toBeNull();
        expect(setCookieHeader).not.toBeUndefined();
        expect(setCookieHeader).toContain('JSESSIONID=');
    });

    test('return 401 when invalid credentials are provided', async ({ loginService }) => {
        const response = await loginService.login({
            email: "a@email.com",
            password: "a1"
        });

        expect(response.status()).toBe(401);
    });

    test('capture 302 before redirect when logging out', async ({ loginService, page }) => {
        await page.route('https://localhost:8443/cs122b_war/login.html', async (route) => {
            const response = await route.fetch({ maxRedirects: 0 });

            expect(response.status()).toBe(302);

            await route.continue();
        });
    });
});