package movies.employees;

public class Employee {
    private final String fullName;
    private final String email;

    public Employee(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
}
