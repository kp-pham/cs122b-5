class HomePage {
    constructor(page) {
        this.page = page;
        this.logoutButton = page.getByRole('button', { name: 'Logout' });
    }
}

export default HomePage;