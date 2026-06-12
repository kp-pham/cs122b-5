import { test, expect } from '../fixtures';

test.describe("login API endpoints", () => {
    test('return 200 and cookie when login successful', async ({ loginService }) => {
        const response = await loginService.login({
            email: "a@email.com",
            password: "a2"
        });

        expect(response.status()).toBe(200);

        const setCookieHeader = response.headers()['set-cookie'];
        
        expect(setCookieHeader).not.toBeNull();
        expect(setCookieHeader).toContain('JSESSIONID=');
    });

    test('return 401 when invalid credentials are provided', async ({ loginService }) => {
        const response = await loginService.login({
            email: "a@email.com",
            password: "a1"
        });

        expect(response.status()).toBe(401);
    });
});