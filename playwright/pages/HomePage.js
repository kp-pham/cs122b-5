class HomePage {
    constructor(page) {
        this.page = page;
        this.logoutButton = page.getByRole('button', { name: 'Logout' });
    }

    async goto() {
        await this.page.goto("https://localhost:8443/cs122b_war/index.html");
    }

    async logout() {
        await this.logoutButton.click();
    }
}

export default HomePage;