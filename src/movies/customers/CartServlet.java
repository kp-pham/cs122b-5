package movies.customers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.NamingException;

import common.RedisUtil;
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

        RedisUtil.init();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String sessionId = RedisUtil.getCookieValue(request, "redisSessionId");
        String redisKey = "cart:" + sessionId;

        Map<String, String> cart = RedisUtil.hgetAll(redisKey);

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT title, price FROM movies WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(query);

            JsonArray jsonArray = new JsonArray();

            BigDecimal total = BigDecimal.ZERO;

            for (Map.Entry<String, String> entry : cart.entrySet()) {
                String movieId = entry.getKey();
                int quantity = Integer.parseInt(entry.getValue());

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");

        String sessionId = RedisUtil.getCookieValue(request, "redisSessionId");
        String redisKey = "cart:" + sessionId;

        String action = request.getParameter("action");
        String movieId = request.getParameter("id");

        switch (action) {
            case "add":
                RedisUtil.hincrBy(redisKey, movieId, 1);
                break;

            case "subtract":
                String value = RedisUtil.hget(redisKey, movieId);

                if (value != null) {
                    int quantity = Integer.parseInt(value);

                    if (quantity > 1) {
                        RedisUtil.hincrBy(redisKey, movieId, -1);
                    } else {
                        RedisUtil.hdel(redisKey, movieId);
                    }
                }

                break;

            case "remove":
                RedisUtil.hdel(redisKey, movieId);
                break;
        }
    }
}
