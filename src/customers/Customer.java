package customers;

public class Customer {
    private final int id;
    private final String email;

    public Customer(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
