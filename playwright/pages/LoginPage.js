class LoginPage {
    constructor(page) {
        this.page = page;
        this.signInHeader = page.getByRole('heading', { name: 'Sign in' });
        this.emailInput = page.getByRole('textbox', { name: 'Email Address' });
        this.passwordInput = page.getByRole('textbox', { name: 'Password' });
        this.loginButton = page.getByRole('button', { name: 'Login' });
    }

    async goto() {
        await this.page.goto('https://localhost:8443/cs122b_war/login.html');
    }

    async login(email, password) {
        await this.emailInput.fill(email);
        await this.passwordInput.fill(password);
        await this.loginButton.click();
    }
};

export default LoginPage;