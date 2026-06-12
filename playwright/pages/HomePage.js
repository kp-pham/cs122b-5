class HomePage {
    constructor(page) {
        this.page = page;
    }

    async goto() {
        await this.page.goto("https://localhost:8443/cs122b_war/index.html");
    }

    async logout() {
        await this.logoutButton().click();
    }

    logoutButton() {
        return this.page.getByRole('button', { name: 'Logout' });
    }
}

export default HomePage;