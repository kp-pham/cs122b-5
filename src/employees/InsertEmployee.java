package employees;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;

import org.jasypt.util.password.StrongPasswordEncryptor;

public class InsertEmployee {
    public static void main(String[] args) throws Exception {
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        String url = System.getenv("DB_URL");

        String query = "INSERT INTO employees (email, password, fullName) VALUES (?, ?, ?)";

        Class.forName(System.getenv("JDBC_DRIVER"));
        Connection conn = DriverManager.getConnection(url, username, password);
        PreparedStatement statement = conn.prepareStatement(query);

        Scanner scanner = new Scanner(System.in);

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String encryptedPassword = new StrongPasswordEncryptor().encryptPassword(scanner.nextLine());

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();

        statement.setString(1, email);
        statement.setString(2, encryptedPassword);
        statement.setString(3, fullName);

        int result = statement.executeUpdate();

        System.out.println("adding new employee completed, " + result + " row(s) affected");
    }
}
