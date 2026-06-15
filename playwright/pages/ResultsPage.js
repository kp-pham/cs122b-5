class ResultsPage {
    constructor(page) {
        this.page = page;
    }

    resultsHeader() {
        return this.page.getByRole('heading', { name: 'Results' });
    }

    addToCartButton() {
        return this.page.getByRole('button', { name: 'Add' });
    }

    movieLink() {
        return this.page.getByRole('link', { name: 'The Terminal' });
    }

    starLink() {
        return this.page.getByRole('link', { name: 'Tom Hanks' });
    }

    addedToCartMessage() {
        return this.page.getByText('Successfully added movie to cart!');
    }
};

export default ResultsPage;