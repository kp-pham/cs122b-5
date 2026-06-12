import { expect } from '@playwright/test';

class LoginPage {
    constructor(page) {
        this.page = page;
        this.signInHeader = page.getByRole('heading', { name: 'Sign in' });
        this.emailTextbox = page.getByRole('textbox', { name: 'Email Address' });
        this.passwordTextbox = page.getByRole('textbox', { name: 'Password' });
        this.loginButton = page.getByRole('button', { name: 'Login' });
    }

    async goto() {
        await this.page.goto('https://localhost:8443/cs122b_war/login.html');
    }

    async login(email, password) {
        await this.emailTextbox.fill(email);
        await this.passwordTextbox.fill(password);
        await this.loginButton.click();
    }
};

export default LoginPage;