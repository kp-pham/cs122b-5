package movies.employees;

import com.google.gson.JsonObject;

import javax.naming.NamingException;
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
import java.sql.Types;
import java.sql.ResultSet;

@WebServlet(name = "movies.employees.SingleStarServlet", urlPatterns = "/api/movies.employees/star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = ConnectionManager.getMasterDataSource();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String name = request.getParameter("name");

        PrintWriter out = response.getWriter();

        if (name == null || name.trim().isEmpty()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Please provide a name.");

            out.write(jsonObject.toString());
            response.setStatus(400);

            out.close();
            return;
        }

        String param = request.getParameter("birthYear");
        Integer birthYear = null;

        if (param != null && !param.trim().isEmpty()) {
            try {
                birthYear = Integer.parseInt(param);

            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", "Please provide a valid birth year.");

                out.write(jsonObject.toString());
                response.setStatus(400);

                out.close();
                return;
            }

            if (birthYear < 0) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", "Please provide a nonnegative number for the birth year.");

                out.write(jsonObject.toString());
                response.setStatus(400);

                out.close();
                return;
            }
        }

        try (Connection conn = dataSource.getConnection()) {
            String id = getId(conn);

            String query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, id);
            statement.setString(2, name);

            if (birthYear == null) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, birthYear);
            }

            statement.executeUpdate();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", id);
            jsonObject.addProperty("name", name);

            if (birthYear != null) {
                jsonObject.addProperty("birthYear", birthYear);
            } else {
                jsonObject.add("birthYear", null);
            }

            jsonObject.addProperty("message", String.format("Star added. starId: %s", id));
            response.setStatus(201);
            response.setHeader("Location", "/api/movies.customers/star?id=" + id);

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

    private String getId(java.sql.Connection conn) throws Exception {
        String query = "SELECT MAX(id) AS id FROM stars";
        PreparedStatement statement = conn.prepareStatement(query);

        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            String id = rs.getString("id");

            String string = id.replaceAll("\\d", "");
            int number = Integer.parseInt(id.replaceAll("[a-zA-z]", ""));

            return string + (number + 1);

        } else {
            throw new Exception("Something went wrong. Please try again.");
        }
    }
}
