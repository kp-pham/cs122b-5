package movies.employees;

import com.google.gson.JsonArray;
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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.sql.ResultSet;

@WebServlet(name = "movies.employees.SingleMovieServlet", urlPatterns="/api/movies.employees/movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    private final String ROLLED_BACK = "ROLLED_BACK";
    private final String DUPLICATE_MOVIE = "DUPLICATE_MOVIE";

    public void init(ServletConfig config) {
        try {
            dataSource = ConnectionManager.getMasterDataSource();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String genreName = request.getParameter("genreName");

        String trimmedTitle = (title == null) ? null : title.trim();
        String trimmedYear = (year == null) ? null : year.trim();
        String trimmedDirector = (director == null) ? null : director.trim();
        String trimmedStarName = (starName == null) ? null : starName.trim();
        String trimmedGenreName = (genreName == null) ? null : genreName.trim();

        boolean hasTitle = (trimmedTitle != null && !trimmedTitle.isEmpty());
        boolean hasYear = (trimmedYear != null && !trimmedYear.isEmpty());
        boolean hasDirector = (trimmedDirector != null && !trimmedDirector.isEmpty());
        boolean hasStarName = (trimmedStarName != null && !trimmedStarName.isEmpty());
        boolean hasGenreName = (trimmedGenreName != null && !trimmedGenreName.isEmpty());

        PrintWriter out = response.getWriter();

        if (!hasTitle || !hasYear || !hasDirector) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Please provide the title, year, and director of the movie.");

            out.write(jsonObject.toString());
            response.setStatus(400);

            return;
        }

        int releaseYear;

        try {
            releaseYear = Integer.parseInt(trimmedYear);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Please provide a valid year.");

            out.write(jsonObject.toString());
            response.setStatus(400);

            return;
        }

        if (releaseYear < 0) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Please provide a nonnegative number for year.");

            out.write(jsonObject.toString());
            response.setStatus(400);

            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "{CALL add_movie(?, ?, ?, ?, ?)}";
            CallableStatement statement = conn.prepareCall(query);

            statement.setString(1, trimmedTitle);
            statement.setInt(2, releaseYear);
            statement.setString(3, trimmedDirector);

            if (hasStarName) {
                statement.setString(4, trimmedStarName);
            } else {
                statement.setNull(4, Types.VARCHAR);
            }

            if (hasGenreName) {
                statement.setString(5, trimmedGenreName);
            } else {
                statement.setNull(5, Types.VARCHAR);
            }

            ResultSet rs = statement.executeQuery();
            rs.next();

            String message = rs.getString("message");

            JsonObject jsonObject = new JsonObject();

            switch (message) {
                case ROLLED_BACK:
                    jsonObject.addProperty("message", rs.getString("error"));
                    response.setStatus(500);
                    break;

                case DUPLICATE_MOVIE:
                    jsonObject.addProperty("message", "Movie already exists.");
                    response.setStatus(409);
                    break;

                default:
                    String movieId = rs.getString("movieId");
                    String starId = rs.getString("starId");
                    int genreId = rs.getInt("genreId");

                    JsonObject movie = new JsonObject();
                    movie.addProperty("id", movieId);
                    movie.addProperty("title", trimmedTitle);
                    movie.addProperty("year", releaseYear);
                    movie.addProperty("director", trimmedDirector);

                    JsonArray genresArray = new JsonArray();
                    genresArray.add(trimmedGenreName);

                    jsonObject.add("genres", genresArray);

                    JsonArray starsArray = new JsonArray();
                    starsArray.add(trimmedStarName);

                    jsonObject.add("stars", starsArray);

                    jsonObject.addProperty("message", String.format("Movie added. movieId: %s starId: %s genreId: %d", movieId, starId, genreId));
                    response.setStatus(201);
                    response.setHeader("Location", "/api/movies.customers/movie?id=" + movieId);
                    break;
            }

            out.write(jsonObject.toString());

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error: ", e);
            response.setStatus(500);

        } finally {
            out.close();
        }
    }
}