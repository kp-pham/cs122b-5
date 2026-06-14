class MoviesService {
    constructor(request) {
        this.request = request;
    }

    async search({ title = null, year = null, director = null, star = null } ) {
        return await this.request.get('https://localhost:8443/cs122b_war/api/customers/search', {
            params: {
                ...(title !== null && { title }),
                ...(year !== null && { year }),
                ...(director !== null && { director }),
                ...(star !== null && { star }),
                sort: 'title-asc-rating-desc',
                page: 1,
                pageSize: 25,
            }
        });
    }

    async cartContents() {
        return await this.request.get('https://localhost:8443/cs122b_war/api/customers/cart');
    }
    
    async addToCart(movieId) {
        return await this.request.post('https://localhost:8443/cs122b_war/api/customers/cart', {
            form: {
                action: 'add',
                id: movieId,
            }
        });
    }

    async placeOrder({ firstName = null, lastName = null, card = null, expiration = null }) {
        return await this.request.post('https://localhost:8443/cs122b_war/api/customers/transactions', {
            form: {
                ...(firstName !== null && { firstName }),
                ...(lastName !== null && { lastName }),
                ...(card !== null && { card }),
                ...(expiration !== null && { expiration }),
            }
        });
    }
};

export default MoviesService;