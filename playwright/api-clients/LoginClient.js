class LoginClient {
    constructor(request) {
        this.request = request;
    }

    async login(payload) {
        return await this.request.post('https://localhost:8443/cs122b_war/api/customers/login', {
            form: payload,
            failOnStatusCode: false,
        });
    }
};

export default LoginClient;