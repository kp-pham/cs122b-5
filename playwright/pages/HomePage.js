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

    async search({ title = null, year = null, director = null, star = null }) {
        if (title != null) 
            await this.titleInput().fill(title);

        if (year != null) 
            await this.yearInput().fill(year);

        if (director != null) 
            await this.directorInput().fill(director);

        if (star != null) 
            await this.starInput().fill(star);

        await this.searchButton().click();
    }

    logoutButton() {
        return this.page.getByRole('button', { name: 'Logout' });
    }

    titleInput() {
        return this.page.getByRole('textbox', { name: 'Title' });
    }

    yearInput() {
        return this.page.getByRole('textbox', { name: 'Year' });
    }

    directorInput() {
        return this.page.getByRole('textbox', { name: 'Director' });
    }

    starInput() {
        return this.page.getByRole('textbox', { name: 'Star' });
    }

    searchButton() {
        return this.page.getByRole('button', { name: 'Search' });
    }
}

export default HomePage;