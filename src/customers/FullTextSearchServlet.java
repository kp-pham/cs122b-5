package customers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.naming.InitialContext;
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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet(name = "customers.FullTextSearchServlet", urlPatterns = "/api/customers/full-text")
public class FullTextSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    private static final String SORT_TITLE_DESC_RATING_ASC = "title-desc-rating-asc";
    private static final String SORT_TITLE_ASC_RATING_ASC = "title-asc-rating-asc";
    private static final String SORT_TITLE_DESC_RATING_DESC = "title-desc-rating-desc";

    private static final String SORT_RATING_ASC_TITLE_DESC = "rating-asc-title-desc";
    private static final String SORT_RATING_DESC_TITLE_ASC = "rating-desc-title-asc";
    private static final String SORT_RATING_ASC_TITLE_ASC = "rating-asc-title-asc";
    private static final String SORT_RATING_DESC_TITLE_DESC = "rating-desc-title-desc";

    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_SIZE = 25;

    Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 25, 50, 100);

    public void init(ServletConfig config) {
        try {
            dataSource = ConnectionManager.getSlaveDataSource();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String q = request.getParameter("q");
        String sort = request.getParameter("sort");
        String page = request.getParameter("page");
        String size = request.getParameter("pageSize");

        String trimmedQuery = (q == null) ? null : q.trim();
        String trimmedPage = (page == null) ? null : page.trim();
        String trimmedSize = (size == null) ? null : size.trim();

        boolean hasQuery = (trimmedQuery != null && !trimmedQuery.isEmpty());
        boolean hasPage = (trimmedPage != null && !trimmedPage.isEmpty());
        boolean hasSize = (trimmedSize != null && !trimmedSize.isEmpty());

        PrintWriter out = response.getWriter();

        if (!hasQuery) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Please enter a search term.");
            out.write(jsonObject.toString());

            response.setStatus(400);
            return;
        }

        int pageNumber = DEFAULT_PAGE_NUMBER;
        int pageSize = DEFAULT_PAGE_SIZE;

        try {
            if (hasPage) {
                pageNumber = Integer.parseInt(page);

                if (pageNumber < 1) {
                    throw new Exception("Please provide a valid page number");
                }
            }

            if (hasSize) {
                pageSize = Integer.parseInt(size);

                if (!ALLOWED_PAGE_SIZES.contains(pageSize)) {
                    throw new Exception("Please provide a valid page size");
                }
            }

        } catch (Exception E) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", "Please provide valid page number and size");
            out.write(jsonObject.toString());

            response.setStatus(400);
            return;
        }

        int offset = (pageNumber - 1) * pageSize;

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT M.id, M.title, M.year, M.director, R.rating, " +
                           "IFNULL( " +
                           "    CONCAT('[', " +
                           "           GROUP_CONCAT(DISTINCT JSON_QUOTE(G.name) ORDER BY G.name ASC), " +
                           "           ']'), " +
                           "    '[]' " +
                           ") AS genres, " +
                           "IFNULL( " +
                           "    CONCAT('[', " +
                           "           GROUP_CONCAT(DISTINCT CASE WHEN S.id IS NOT NULL THEN JSON_OBJECT('id', S.id, 'name', S.name) END " +
                           "                        ORDER BY S.movie_count DESC, S.name ASC), " +
                           "           ']'), " +
                           "    '[]' " +
                           ") AS stars " +
                           "FROM movies AS M " +
                           "LEFT JOIN genres_in_movies AS GIM ON M.id = GIM.movieId " +
                           "LEFT JOIN genres AS G ON GIM.genreId = G.id " +
                           "LEFT JOIN stars_in_movies AS SIM ON M.id = SIM.movieId " +
                           "LEFT JOIN (" +
                           "    SELECT S.id, S.name, COUNT(SIM.movieId) AS movie_count " +
                           "    FROM stars_in_movies AS SIM " +
                           "    LEFT JOIN stars AS S ON SIM.starId = S.id " +
                           "    GROUP BY S.id " +
                           ") AS S ON SIM.starId = S.id " +
                           "LEFT JOIN ratings AS R ON R.movieId = M.id " +
                           "WHERE M.title = ? " +
                           "OR MATCH (M.title) AGAINST (? IN BOOLEAN MODE) " +
                           "OR M.title LIKE CONCAT('%', ?, '%') " +
                           "OR edth(LOWER(?), LOWER(M.title), ?) " +
                           "GROUP BY M.id, M.title, M.year, M.director, R.rating ";

            switch(sort) {
                case SORT_TITLE_DESC_RATING_ASC:
                    query += "ORDER BY M.title DESC, R.rating ASC ";
                    break;

                case SORT_TITLE_ASC_RATING_ASC:
                    query += "ORDER BY M.title ASC, R.rating ASC ";
                    break;

                case SORT_TITLE_DESC_RATING_DESC:
                    query += "ORDER BY M.title DESC, R.rating DESC ";
                    break;

                case SORT_RATING_ASC_TITLE_DESC:
                    query += "ORDER BY R.rating ASC, M.title DESC ";
                    break;

                case SORT_RATING_DESC_TITLE_ASC:
                    query += "ORDER BY R.rating DESC, M.title ASC ";
                    break;

                case SORT_RATING_ASC_TITLE_ASC:
                    query += "ORDER BY R.rating ASC, M.title ASC ";
                    break;

                case SORT_RATING_DESC_TITLE_DESC:
                    query += "ORDER BY R.rating DESC, M.title DESC ";
                    break;

                default:
                    query += "ORDER BY M.title ASC, R.rating DESC ";
                    break;
            }

            query += "LIMIT ? OFFSET ?";

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

            // Allow 25% of the characters to be incorrect
            int threshold = Math.max(1, trimmedQuery.length() / 4);

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, trimmedQuery);
            statement.setString(2, entry);
            statement.setString(3, trimmedQuery);
            statement.setString(4, trimmedQuery);
            statement.setInt(5, threshold);
            statement.setInt(6, pageSize + 1);
            statement.setInt(7, offset);

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("id", rs.getString("M.id"));
                jsonObject.addProperty("title", rs.getString("M.title"));
                jsonObject.addProperty("year", rs.getString("M.year"));
                jsonObject.addProperty("director", rs.getString("M.director"));
                jsonObject.addProperty("rating", rs.getString("R.rating"));

                JsonArray genresArray = JsonParser.parseString(rs.getString("genres")).getAsJsonArray();
                jsonObject.add("genres", genresArray);

                JsonArray starsArray = JsonParser.parseString(rs.getString("stars")).getAsJsonArray();
                jsonObject.add("stars", starsArray);

                jsonArray.add(jsonObject);
            }

            JsonObject jsonObject = new JsonObject();

            if (jsonArray.size() > pageSize) {
                jsonObject.addProperty("lastPage", false);
                jsonObject.addProperty("outOfBounds", false);
                jsonArray.remove(jsonArray.size() - 1);

            } else if (jsonArray.isEmpty()) {
                jsonObject.addProperty("lastPage", true);
                jsonObject.addProperty("outOfBounds", true);

            } else {
                jsonObject.addProperty("lastPage", true);
                jsonObject.addProperty("outOfBounds", false);
            }

            jsonObject.add("results", jsonArray);

            rs.close();
            statement.close();

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
