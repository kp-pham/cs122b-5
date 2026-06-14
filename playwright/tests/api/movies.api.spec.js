import { test, expect } from '../fixtures/base.fixture';

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

    test('shopping cart lifecycle', async ({ moviesService }) => {
        // First copy of 'The Terminal' added to cart
        let cartActionResponse = await moviesService.addToCart('tt0362227');
        expect(cartActionResponse.status()).toBe(200);

        let cartContentsResponse = await moviesService.cartContents();
        expect(cartContentsResponse.status()).toBe(200);

        let cartContents = await cartContentsResponse.json();
        expect(cartContents).toMatchObject({
            items: expect.arrayContaining([
                expect.objectContaining({ id: 'tt0362227', title: 'The Terminal' }),
            ]),
            total: 13.99,
        });

        // Second copy of 'The Terminal' added to cart
        cartActionResponse = await moviesService.addToCart('tt0362227');
        expect(cartActionResponse.status()).toBe(200);

        cartContentsResponse = await moviesService.cartContents();
        expect(cartActionResponse.status()).toBe(200);

        cartContents = await cartContentsResponse.json();
        expect(cartContents).toMatchObject({
            items: expect.arrayContaining([
                expect.objectContaining({ 
                    id: 'tt0362227', 
                    title: 'The Terminal', 
                    quantity: 2, 
                }),
            ]),
            total: 27.98,
        });
        
        // First copy of 'The Final Season' added to cart
        cartActionResponse = await moviesService.addToCart('tt0449018');
        expect(cartActionResponse.status()).toBe(200);

        cartContentsResponse = await moviesService.cartContents();
        expect(cartActionResponse.status()).toBe(200);

        cartContents = await cartContentsResponse.json();
        expect(cartContents).toMatchObject({
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

        // Checkout with invalid payment information
        let placeOrderResponse = await moviesService.placeOrder({
            firstName: 'Tolly',
            lastName: 'Zhang',
            card: '06137888888878061388',
            expiration: '2026-06-13'
        });

        expect(placeOrderResponse.status()).toBe(400);

        // Order placed after checkout with valid payment information
        placeOrderResponse = await moviesService.placeOrder({
            firstName: 'Tolly',
            lastName: 'Zhang',
            card: '06137888888878061388',
            expiration: '2005-06-13'
        });

        expect(placeOrderResponse.status()).toBe(200);

        let orderDetailsResponse = await moviesService.orderDetails();
        expect(orderDetailsResponse.status()).toBe(200);

        let orderDetails = await orderDetailsResponse.json();
        expect(orderDetails).toMatchObject({ total: 48.97 });
    });
});