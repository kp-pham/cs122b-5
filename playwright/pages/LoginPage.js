class LoginPage {
    constructor(page) {
        this.page = page;
    }

    async goto() {
        await this.page.goto('https://localhost:8443/cs122b_war/login.html');
    }

    async login(email, password) {
        await this.emailInput().fill(email);
        await this.passwordInput().fill(password);
        await this.loginButton().click();
    }

    signInHeader() {
        return this.page.getByRole('heading', { name: 'Sign in' });
    }

    emailInput() {
        return this.page.getByRole('textbox', { name: 'Email Address' });
    }

    passwordInput() {
        return this.page.getByRole('textbox', { name: 'Password' });
    }

    loginButton() {
        return this.page.getByRole('button', { name: 'Login' });
    }
};

export default LoginPage;