package customers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.ConnectionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

@WebServlet(name = "customers.TransactionServlet", urlPatterns = "/api/transactions")
public class TransactionServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = ConnectionManager.getMasterDataSource();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();

        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return;
        }

        PrintWriter out = response.getWriter();

        BigDecimal total = BigDecimal.ZERO;

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT price FROM movies WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(query);

            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                String movieId = entry.getKey();
                int quantity = entry.getValue();

                statement.setString(1, movieId);
                ResultSet rs = statement.executeQuery();

                if (!rs.next()) continue;

                BigDecimal price = rs.getBigDecimal("price");

                BigDecimal subtotal = price.multiply(new BigDecimal(quantity));
                total = total.add(subtotal);
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("total", total.doubleValue());

            out.write(jsonObject.toString());
            response.setStatus(200);

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

    // Separate transaction validation from order confirmation (create new servlet)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();

        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return;
        }

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String card = request.getParameter("card");
        String expiration = request.getParameter("expiration");

        String trimmedFirstName = (firstName == null) ? null : firstName.trim();
        String trimmedLastName = (lastName == null) ? null : lastName.trim();
        String trimmedCard = (card == null) ? null : card.trim();
        String trimmedExpiration = (expiration == null) ? null : expiration.trim();

        boolean hasFirstName = (trimmedFirstName != null && !trimmedFirstName.isEmpty());
        boolean hasLastName = (trimmedLastName != null && !trimmedLastName.isEmpty());
        boolean hasCard = (trimmedCard != null && !trimmedCard.isEmpty());
        boolean hasExpiration = (trimmedExpiration != null && !trimmedExpiration.isEmpty());

        PrintWriter out = response.getWriter();

        if (!hasFirstName || !hasLastName || !hasCard || !hasExpiration) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Please provide payment information");
            out.write(jsonObject.toString());

            response.setStatus(400);
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // Enforce atomicity for transactions

            String query = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastname = ? AND expiration = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, trimmedCard);
            statement.setString(2, trimmedFirstName);
            statement.setString(3, trimmedLastName);
            statement.setDate(4, Date.valueOf(trimmedExpiration));

            ResultSet rs =  statement.executeQuery();

            if (!rs.next()) {
                JsonObject jsonObject = new JsonObject();

                request.getServletContext().log("Transaction not processed");
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "Invalid payment information");

                out.write(jsonObject.toString());
                response.setStatus(400);

                return;
            }

            String insertQuery = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStatement = conn.prepareStatement(insertQuery, statement.RETURN_GENERATED_KEYS);

            Customer customer = (Customer) session.getAttribute("customer");
            int customerId = customer.getId();

            Date date = new Date(System.currentTimeMillis());

            BigDecimal total =  BigDecimal.ZERO;

            JsonArray jsonArray = new JsonArray();

            try {
                for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                    String movieId = entry.getKey();
                    int quantity = entry.getValue();

                    String selectQuery = "SELECT title, price FROM movies WHERE id = ?";

                    PreparedStatement selectStatement = conn.prepareStatement(selectQuery);
                    selectStatement.setString(1, movieId);

                    ResultSet results = selectStatement.executeQuery();

                    if (!results.next())
                        continue;

                    String title = results.getString("title");
                    BigDecimal price = results.getBigDecimal("price");

                    BigDecimal subtotal = price.multiply(new BigDecimal(quantity));
                    subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

                    total = total.add(subtotal);

                    insertStatement.setInt(1, customerId);
                    insertStatement.setString(2, movieId);
                    insertStatement.setDate(3, date);
                    insertStatement.setInt(4, quantity);

                    insertStatement.executeUpdate(); // Throws SQLException when insertion fails
                    ResultSet keys = insertStatement.getGeneratedKeys();

                    keys.next();
                    int saleId = keys.getInt(1);

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("saleId", saleId);
                    jsonObject.addProperty("movieId", movieId);
                    jsonObject.addProperty("title", title);
                    jsonObject.addProperty("quantity", quantity);
                    jsonObject.addProperty("price", price.setScale(2, RoundingMode.HALF_UP).doubleValue());
                    jsonObject.addProperty("subtotal", subtotal.doubleValue());

                    jsonArray.add(jsonObject);
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback(); // Sales records not inserted when transaction fails
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("sales", jsonArray);
            jsonObject.addProperty("total", total.doubleValue());

            out.write(jsonObject.toString());
            response.setStatus(200);

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
