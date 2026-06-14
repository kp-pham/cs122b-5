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

    test('cart stores more than one copy of the same movie', async ({ moviesService }) => {
        const cartActionResponse = await moviesService.addToCart('tt0362227');
        expect(cartActionResponse.status()).toBe(200);

        const cartContentsResponse = await moviesService.cartContents();
        expect(cartActionResponse.status()).toBe(200);

        const body = await cartContentsResponse.json();
        expect(body).toMatchObject({
            items: expect.arrayContaining([
                expect.objectContaining({ 
                    id: 'tt0362227', 
                    title: 'The Terminal', 
                    quantity: 2, 
                }),
            ]),
            total: 27.98,
        });
    });

    test('cart stores separate copies for different movies', async ({ moviesService }) => {
        const cartActionResponse = await moviesService.addToCart('tt0449018');
        expect(cartActionResponse.status()).toBe(200);

        const cartContentsResponse = await moviesService.cartContents();
        expect(cartActionResponse.status()).toBe(200);

        const body = await cartContentsResponse.json();
        expect(body).toMatchObject({
            items: expect.arrayContaining([
                expect.objectContaining({ 
                    id: 'tt0362227', 
                    title: 'The Terminal', 
                    quantity: 2,
                }),

                expect.objectContaining({
                    id: 'tt0449018',
                    title: 'The Final Season',
                    quantity: 1
                })
            ]),
            total: 48.97,
        });
    });

    test('returns 400 when invalid payment information provided', async ({ moviesService }) => {
        const response = await moviesService.placeOrder({
            firstName: 'Tolly',
            lastName: 'Zhang',
            card: '06137888888878061388',
            expiration: '2026-06-13'
        });

        expect(response.status()).toBe(400);
    });

    test('correct order details when order is placed', async ({ moviesService }) => {
        const placeOrderResponse = await moviesService.placeOrder({
            firstName: 'Tolly',
            lastName: 'Zhang',
            card: '06137888888878061388',
            expiration: '2005-06-13'
        });

        expect(placeOrderResponse.status()).toBe(200);

        const orderDetailsResponse = await moviesService.orderDetails();
        expect(orderDetailsResponse.status()).toBe(200);

        const body = await orderDetailsResponse.json();
        expect(body).toMatchObject({ total: 48.97 });
    });
});