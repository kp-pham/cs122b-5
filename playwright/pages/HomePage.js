class HomePage {
    constructor(page) {
        this.page = page;
        this.logoutButton = page.getByRole('button', { name: 'Logout' });
    }

    async logout() {
        await this.logoutButton.click();
    }
}

export default HomePage;