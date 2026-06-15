import { test, expect } from './fixtures/base.fixture';

test.describe('customer searches and purchases movies', () => {
    test('correct search results when customer searches for movie', async ({ homePage, resultsPage }) => {
        await homePage.goto();
        await homePage.search({ title: 'term', star: 'tom' });
        
        await expect(resultsPage.resultsHeader()).toBeVisible();
        await expect(resultsPage.addToCartButton()).toBeVisible();
        await expect(resultsPage.movieLink()).toBeVisible();
        await expect(resultsPage.movieLink()).toHaveAttribute('href', 'single-movie.html?id=tt0362227');
        await expect(resultsPage.starLink()).toBeVisible();
        await expect(resultsPage.starLink()).toHaveAttribute('href', 'single-star.html?id=nm0000158');
    });

    test('shows alert message when movie added to cart', async ({ homePage, resultsPage }) => {
        await homePage.goto();
        await homePage.search({ title: 'term', star: 'tom' });

        await resultsPage.addToCartButton().click();
        await expect(resultsPage.addedToCartMessage()).toBeVisible();
    });

    test('shows alert message when invalid payment information provided', async ({ homePage, resultsPage }) => {
        await homePage.goto();
        await homePage.search({ title: 'term', star: 'tom' });

        await resultsPage.addToCartButton().click();
    });
});