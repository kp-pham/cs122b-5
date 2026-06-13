import { test, expect } from '../fixtures/base.fixture';

test.describe.configure({ mode: 'serial' });

test.describe('movies service API endpoints', () => {
    test('basic search returns correct results', async ({ moviesService }) => {
        const response = await moviesService.search({ title: 'term', star: 'tom' });
        expect(response.status()).toBe(200);
        
        const body = await response.json();

        expect(body).toMatchObject({
            lastPage: true,
            outOfBounds: false,
            results: [
                {
                    id: 'tt0362227',
                    title: 'The Terminal',
                    year: '2004',
                    director: 'Steven Spielberg',
                    rating: '7.3',
                    genres: expect.arrayContaining(['Comedy', 'Drama', 'Romance']),
                    stars: expect.arrayContaining([
                        expect.objectContaining({ name: 'Walter F. Parkes' }),
                        expect.objectContaining({ name: 'Tom Hanks' }),
                        expect.objectContaining({ name: 'Laurie MacDonald' })
                    ])
                }
            ]
        });
    });

    test('empty cart has no items and zero total price', async ({ moviesService }) => {
        const response = await moviesService.cartContents();
        expect(response.status()).toBe(200);

        const body = await response.json();
        expect(body).toMatchObject({ items: [], total: 0.00 });
    });

    test('cart contents updated when movie added to cart', async ({ moviesService }) => {
        const cartActionResponse = await moviesService.addToCart('tt0362227');
        expect(cartActionResponse.status()).toBe(200);

        const cartContentsResponse = await moviesService.cartContents();
        expect(cartActionResponse.status()).toBe(200);

        const body = await cartContentsResponse.json();
        expect(body).toMatchObject({
            items: expect.arrayContaining([
                expect.objectContaining({ id: 'tt0362227', title: 'The Terminal' }),
            ]),
            total: 13.99,
        });
    });
});