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

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

import utils.ConnectionManager;

@WebServlet(name = "customers.CartServlet", urlPatterns = "/api/customers/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = ConnectionManager.getSlaveDataSource();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();

        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT title, price FROM movies WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(query);

            JsonArray jsonArray = new JsonArray();

            BigDecimal total = BigDecimal.ZERO;

            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                String movieId = entry.getKey();
                int quantity = entry.getValue();

                statement.setString(1, movieId);
                ResultSet rs = statement.executeQuery();

                if (!rs.next()) continue;

                String title = rs.getString("title");
                BigDecimal price = rs.getBigDecimal("price");

                BigDecimal subtotal = price.multiply(new BigDecimal(quantity));
                subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

                total = total.add(subtotal);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", movieId);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("quantity", quantity);
                jsonObject.addProperty("price", price.setScale(2, RoundingMode.HALF_UP).doubleValue());
                jsonObject.addProperty("subtotal", subtotal.doubleValue());

                jsonArray.add(jsonObject);
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("items", jsonArray);
            jsonObject.addProperty("total", total.doubleValue());

            out.write(jsonObject.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);

        } finally {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        HttpSession session = request.getSession();

        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        String action = request.getParameter("action");
        String movieId = request.getParameter("id");

        synchronized(cart) {
            switch (action) {
                case "add":
                    cart.put(movieId, cart.getOrDefault(movieId, 0) + 1);
                    break;

                case "subtract":
                    int quantity = cart.getOrDefault(movieId, 0);

                    if (quantity > 1) {
                        cart.put(movieId, cart.getOrDefault(movieId, 0) - 1);
                    }

                    break;

                case "remove":
                    cart.remove(movieId);
                    break;
            }
        }

        doGet(request, response);
    }
}
