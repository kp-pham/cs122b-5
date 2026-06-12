package customers;

import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import utils.ConnectionManager;


@WebServlet(name = "customers.LoginServlet", urlPatterns = "/api/customers/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = ConnectionManager.getSlaveDataSource();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        PrintWriter out = response.getWriter();

//        try {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//
//        } catch (Exception e) {
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("message", "reCAPTCHA verification failed. Please try again.");
//            out.write(jsonObject.toString());
//
//            request.getServletContext().log("Error:", e);
//            response.setStatus(400);
//
//            out.close();
//            return;
//        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT id, password FROM customers WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

            if (rs.next() && passwordEncryptor.checkPassword(password, rs.getString("password"))) {
                int id = rs.getInt("id");
                request.getSession().setAttribute("customer", new Customer(id, email));
                jsonObject.addProperty("status", "success");
                jsonObject.addProperty("message", "success");
                response.setStatus(200);

            } else {
                request.getServletContext().log("Login failed");
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "Incorrect username or password");
                response.setStatus(401);
            }

            out.write(jsonObject.toString());

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);

        } finally {
            out.close();
        }
    }
}
