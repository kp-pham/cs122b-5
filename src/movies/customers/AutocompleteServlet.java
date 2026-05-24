package movies.customers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

import utils.ConnectionManager;

@WebServlet(name = "movies.customers.AutocompleteServlet", urlPatterns="/api/movies.customers/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long SerialVersionUID = 2L;

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
        PrintWriter out = response.getWriter();

        JsonArray jsonArray = new JsonArray();

        String q = request.getParameter("q");

        String trimmedQuery = (q == null) ? null : q.trim();
        boolean hasQuery = (trimmedQuery != null && !trimmedQuery.isEmpty());

        if (!hasQuery || trimmedQuery.length() < 3) {
            out.write(jsonArray.toString());
            response.setStatus(200);

            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT M.id, M.title " +
                           "FROM movies AS M " +
                           "WHERE M.title = ? " +
                           "OR MATCH (M.title) AGAINST (? IN BOOLEAN MODE) " +
                           "OR M.title LIKE CONCAT('%', ?, '%') " +
                           "OR edth(LOWER(?), LOWER(M.title), ?) " +
                           "LIMIT 10";

            PreparedStatement statement = conn.prepareStatement(query);

            String[] tokens = trimmedQuery.split("\\s+");

            StringBuilder logicalOperators = new StringBuilder();

            for (int i = 0; i < tokens.length; ++i) {
                logicalOperators.append("+")
                                .append(tokens[i])
                                .append("*");

                if (i < tokens.length - 1) {
                    logicalOperators.append(" ");
                }
            }

            String entry = logicalOperators.toString();

            int threshold = Math.max(1, trimmedQuery.length() / 4);

            statement.setString(1, trimmedQuery);
            statement.setString(2, entry);
            statement.setString(3, trimmedQuery);
            statement.setString(4, trimmedQuery);
            statement.setInt(5, threshold);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("value", rs.getString("M.title"));

                JsonObject data = new JsonObject();
                data.addProperty("id", rs.getString("M.id"));

                jsonObject.add("data", data);

                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();

            out.write(jsonArray.toString());
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
