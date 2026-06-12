import { expect } from '@playwright/test';

class LoginPage {
    constructor(page) {
        this.page = page;
        this.signInHeader = page.getByRole('heading', { name: 'Sign in' });
        this.emailTextbox = page.getByRole('textbox', { name: 'Email Address' });
        this.passwordTextbox = page.getByRole('textbox', { name: 'Password' });
        this.signInButton = page.getByRole('button', { name: 'Login' });
    }
};

export default Playwright;