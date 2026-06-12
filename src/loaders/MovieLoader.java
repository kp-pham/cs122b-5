package loaders;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MovieLoader extends DataLoader {
    public MovieLoader(Connection conn) {
        super(conn);
    }

    @Override
    protected void createStagingTable() throws SQLException {
        String dropQuery = "DROP TABLE IF EXISTS movies_staging";
        String createQuery = "CREATE TABLE movies_staging(" +
                             "    id TEXT, " +
                             "    title TEXT, " +
                             "    year TEXT, " +
                             "    director TEXT" +
                             ")";

        PreparedStatement statement = conn.prepareStatement(dropQuery);
        statement.executeUpdate();

        statement = conn.prepareStatement(createQuery);
        statement.executeUpdate();

        statement.close();
    }

    @Override
    protected void loadToStaging(String file) throws SQLException {
        String query = "LOAD DATA LOCAL INFILE ? " +
                       "INTO TABLE movies_staging " +
                       "FIELDS TERMINATED BY ',' " +
                       "ENCLOSED BY '\"' " +
                       "LINES TERMINATED BY '\\n' " +
                       "IGNORE 1 ROWS " +
                       "(@id, @title, @year, @director) " +
                       "SET " +
                       "    id = TRIM(@id), " +
                       "    title = TRIM(@title), " +
                       "    year = TRIM(@year), " +
                       "    director = TRIM(@director)";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, file);

        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void validateAndTransform() throws SQLException {
        String query = "INSERT INTO movies (id, title, year, director, price) " +
                       "WITH deduped AS (" +
                       "   SELECT id " +
                       "   FROM movies_staging " +
                       "   GROUP BY id " +
                       "   HAVING COUNT(*) = 1 " +
                       "), " +
                       "cleaned AS ( " +
                       "    SELECT S.id, S.title, S.year, S.director " +
                       "    FROM movies_staging AS S " +
                       "    INNER JOIN deduped AS D ON D.id = S.id " +
                       "    WHERE S.id IS NOT NULL AND S.id != '' " +
                       "    AND S.title IS NOT NULL and S.title != '' " +
                       "    AND S.year IS NOT NULL AND S.year != '' AND S.year REGEXP '^[0-9]+$' " +
                       "    AND S.director IS NOT NULL AND S.director != '' " +
                       ") " +
                       "SELECT C.id, C.title, CAST(C.year AS UNSIGNED), C.director, " +
                       "       FLOOR(1 + RAND() * 30) + ELT(FLOOR(1 + RAND() * 3), 0.99, 0.49, 0.00) " +
                       "FROM cleaned AS C " +
                       "LEFT JOIN movies AS M ON M.id = C.id " +
                       "WHERE M.id IS NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void reportErrors() throws SQLException {
        String query = "WITH dupes AS ( " +
                       "    SELECT id " +
                       "    FROM movies_staging " +
                       "    GROUP BY id " +
                       "    HAVING COUNT(*) > 1 " +
                       ") " +
                       "SELECT S.id, S.title, S.year, S.director, " +
                       "CASE " +
                       "    WHEN S.id IS NULL OR S.id = '' THEN 'Invalid or missing id' " +
                       "    WHEN S.title IS NULL OR S.title = '' THEN 'Invalid or missing title' " +
                       "    WHEN S.year IS NULL OR S.year = '' OR S.year NOT REGEXP '^[0-9]+$' THEN 'Invalid or missing year' " +
                       "    WHEN S.director IS NULL OR S.director = '' THEN 'Invalid or missing director' " +
                       "    WHEN D.id IS NOT NULL THEN 'Duplicate in file' " +
                       "    WHEN M.id IS NOT NULL THEN 'Movie already exists in database' " +
                       "END AS error " +
                       "FROM movies_staging AS S " +
                       "LEFT JOIN dupes AS D ON D.id = S.id " +
                       "LEFT JOIN movies AS M ON M.id = S.id " +
                       "WHERE S.id IS NULL OR S.id = '' " +
                       "OR S.title IS NULL OR S.title = '' " +
                       "OR S.year IS NULL OR S.year = '' OR S.year NOT REGEXP '^[0-9]+$' " +
                       "OR S.director IS NULL OR S.director = '' " +
                       "OR D.id IS NOT NULL " +
                       "OR M.id IS NOT NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            System.out.printf("%s: %s, %s, %s, %s%n",
                              rs.getString("error"),
                              rs.getString("id"),
                              rs.getString("title"),
                              rs.getString("year"),
                              rs.getString("director")
            );
        }

        rs.close();
        statement.close();
    }

    @Override
    protected void deleteStagingTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS movies_staging";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }
}
