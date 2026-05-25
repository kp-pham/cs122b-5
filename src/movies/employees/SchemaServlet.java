package movies.employees;

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

@WebServlet(name = "employees.SchemaServlet", urlPatterns="/api/employees/schema")
public class SchemaServlet extends HttpServlet {
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

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT TABLE_NAME, " +
                           "JSON_ARRAYAGG(JSON_OBJECT('name', COLUMN_NAME, 'type', DATA_TYPE)) AS columns " +
                           "FROM INFORMATION_SCHEMA.COLUMNS " +
                           "WHERE TABLE_SCHEMA = ? " +
                           "GROUP BY TABLE_NAME " +
                           "ORDER BY TABLE_NAME";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, System.getenv("DATABASE"));

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("name", rs.getString("TABLE_NAME"));

                JsonArray columns = JsonParser.parseString(rs.getString("columns")).getAsJsonArray();
                jsonObject.add("columns", columns);

                jsonArray.add(jsonObject);
            }

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
