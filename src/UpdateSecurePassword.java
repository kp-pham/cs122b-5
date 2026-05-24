import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePassword {
    public static void main(String[] args) throws Exception {
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        String url = System.getenv("DB_URL");

        Class.forName(System.getenv("JDBC_DRIVER"));
        Connection conn = DriverManager.getConnection(url, username, password);
        Statement statement = conn.createStatement();

        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

        String query = "SELECT id, password FROM customers";

        ResultSet rs = statement.executeQuery(query);

        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueries = new ArrayList<>();

        while (rs.next()) {
            String id = rs.getString("id");
            String plaintext = rs.getString("password");

            String encryptedPassword = passwordEncryptor.encryptPassword(plaintext);

            String updateQuery = String.format("UPDATE movies.customers SET password = '%s' WHERE id = %s", encryptedPassword, id);
            updateQueries.add(updateQuery);
        }

        rs.close();

        int count = 0;

        for (String updateQuery : updateQueries) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }

        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
        conn.close();
    }
}
