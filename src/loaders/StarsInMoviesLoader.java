package loaders;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StarsInMoviesLoader extends DataLoader {
    public StarsInMoviesLoader(Connection conn) {
        super(conn);
    }

    @Override
    protected void createStagingTable() throws SQLException {
        String dropQuery = "DROP TABLE IF EXISTS stars_in_movies_staging";
        String createQuery = "CREATE TABLE stars_in_movies_staging( " +
                             "    starId TEXT, " +
                             "    movieId TEXT " +
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
                       "INTO TABLE stars_in_movies_staging " +
                       "FIELDS TERMINATED BY ',' " +
                       "ENCLOSED BY '\"' " +
                       "LINES TERMINATED BY '\\n' " +
                       "IGNORE 1 ROWS " +
                       "(@starId, @movieId) " +
                       "SET " +
                       "    starId = TRIM(@starId), " +
                       "    movieId = TRIM(@movieId)";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, file);

        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void validateAndTransform() throws SQLException {
        String query = "INSERT INTO stars_in_movies (starId, movieId) " +
                       "WITH deduped AS ( " +
                       "    SELECT starId, movieId " +
                       "    FROM stars_in_movies_staging " +
                       "    GROUP BY starId, movieId " +
                       "    HAVING COUNT(*) = 1 " +
                       "), " +
                       "cleaned AS ( " +
                       "    SELECT S.starId, S.movieId " +
                       "    FROM stars_in_movies_staging AS S " +
                       "    INNER JOIN deduped AS D ON D.starId = S.starId" +
                       "                           AND D.movieId = S.movieId" +
                       "    WHERE S.starId IS NOT NULL AND S.starId != '' " +
                       "    AND S.movieId IS NOT NULL AND S.movieId != '' " +
                       ") " +
                       "SELECT C.starId, C.movieId " +
                       "FROM cleaned AS C " +
                       "LEFT JOIN stars AS S ON S.id = C.starId " +
                       "LEFT JOIN movies AS M ON M.id = C.movieId " +
                       "WHERE S.id IS NOT NULL " +
                       "AND M.id IS NOT NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void reportErrors() throws SQLException {
        String query = "WITH dupes AS ( " +
                       "    SELECT starId, movieId " +
                       "    FROM stars_in_movies_staging AS S " +
                       "    GROUP BY starId, movieId " +
                       "    HAVING COUNT(*) > 1 " +
                       ") " +
                       "SELECT S.starId, S.movieId, " +
                       "CASE " +
                       "    WHEN S.starId IS NULL OR S.starId = '' THEN 'Invalid or missing star id' " +
                       "    WHEN S.movieId IS NULL OR S.movieId = '' THEN 'Invalid or missing movie id' " +
                       "    WHEN D.starID IS NOT NULL AND D.movieId IS NOT NULL THEN 'Duplicate in file' " +
                       "    WHEN stars.id IS NULL THEN 'Star id references nonexistent star' " +
                       "    WHEN M.id IS NULL THEN 'Movie id references nonexistent movie' " +
                       "END AS error " +
                       "FROM stars_in_movies_staging AS S " +
                       "LEFT JOIN dupes AS D ON D.starId = S.starId " +
                       "                    AND D.movieId = S.movieId " +
                       "LEFT JOIN stars ON stars.id = S.starId " +
                       "LEFT JOIN movies AS M ON M.id = S.movieId " +
                       "WHERE S.starId IS NULL OR S.starId = '' " +
                       "OR S.movieId IS NULL OR S.movieId = '' " +
                       "OR D.starId IS NOT NULL AND D.movieId IS NOT NULL " +
                       "OR stars.id IS NULL " +
                       "OR M.id IS NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            System.out.printf("%s: %s, %s%n",
                              rs.getString("error"),
                              rs.getString("starId"),
                              rs.getString("movieId")
            );
        }

        rs.close();
        statement.close();
    }

    @Override
    protected void deleteStagingTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS stars_in_movies_staging";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }
}
