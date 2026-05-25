package movies.customers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.naming.NamingException;

import common.RedisUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ConnectionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "customers.SingleStarServlet", urlPatterns= "/api/customers/star")
public class SingleStarServlet extends HttpServlet {
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

        String id = request.getParameter("id");

        request.getServletContext().log("getting star id: " + id);

        PrintWriter out = response.getWriter();

        // try-with-resouces implements AutoCloseable interface to automatically close connection
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT S.id, S.name, S.birthYear, " +
                           "IFNULL( " +
                           "    CONCAT('[', " +
                           "           GROUP_CONCAT(DISTINCT CASE WHEN M.id IS NOT NULL THEN JSON_OBJECT('id', M.id, 'title', M.title, 'year', M.year, 'director', M.director) END " +
                           "                        ORDER BY M.year DESC, M.title ASC), " +
                           "           ']'), " +
                           "    '[]' " +
                           ") AS movies " +
                           "FROM stars AS S " +
                           "LEFT JOIN stars_in_movies AS SIM ON S.id = SIM.starId " +
                           "LEFT JOIN movies AS M ON SIM.movieId = M.id " +
                           "WHERE S.id = ? " +
                           "GROUP BY S.id, S.name, S.birthYear";

            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, id);

            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            if (rs.next()) {
                jsonObject.addProperty("id", rs.getString("S.id"));
                jsonObject.addProperty("name", rs.getString("S.name"));
                jsonObject.addProperty("birthYear", rs.getString("S.birthYear"));

                JsonArray moviesArray = JsonParser.parseString(rs.getString("movies")).getAsJsonArray();
                jsonObject.add("movies", moviesArray);
            }

            rs.close();
            statement.close();

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
}