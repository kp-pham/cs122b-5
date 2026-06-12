package loaders;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GenresInMoviesLoader extends DataLoader {
    public GenresInMoviesLoader(Connection conn) {
        super(conn);
    }

    @Override
    protected void createStagingTable() throws SQLException {
        String dropQuery = "DROP TABLE IF EXISTS genres_in_movies_staging";
        String createQuery = "CREATE TABLE genres_in_movies_staging( " +
                             "    genreId TEXT, " +
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
                       "INTO TABLE genres_in_movies_staging " +
                       "FIELDS TERMINATED BY ',' " +
                       "ENCLOSED BY '\"' " +
                       "LINES TERMINATED BY '\\n' " +
                       "IGNORE 1 ROWS " +
                       "(@genreId, @movieId) " +
                       "SET " +
                       "    genreId = TRIM(@genreId), " +
                       "    movieId = TRIM(@movieId)";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, file);

        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void validateAndTransform() throws SQLException {
        String query = "INSERT INTO genres_in_movies (genreId, movieId) " +
                       "WITH deduped AS ( " +
                       "    SELECT genreId, movieId " +
                       "    FROM genres_in_movies_staging " +
                       "    GROUP BY genreId, movieId " +
                       "    HAVING COUNT(*) = 1 " +
                       "), " +
                       "cleaned AS ( " +
                       "    SELECT CAST(S.genreId AS UNSIGNED) AS genreId, S.movieId " +
                       "    FROM genres_in_movies_staging AS S " +
                       "    INNER JOIN deduped AS D ON D.genreId = S.genreId " +
                       "                           AND D.movieId = S.movieId " +
                       "    WHERE S.genreId IS NOT NULL AND S.genreId != '' AND S.genreId REGEXP '^[0-9]+$' " +
                       "    AND S.movieId IS NOT NULL AND S.movieID != '' " +
                       ") " +
                       "SELECT C.genreId, C.movieId " +
                       "FROM cleaned AS C " +
                       "LEFT JOIN genres AS G ON G.id = C.genreId " +
                       "LEFT JOIN movies AS M ON M.id = C.movieId " +
                       "WHERE G.id IS NOT NULL " +
                       "AND M.id IS NOT NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void reportErrors() throws SQLException {
        String query = "WITH dupes AS ( " +
                       "    SELECT genreId, movieId " +
                       "    FROM genres_in_movies_staging AS S " +
                       "    GROUP BY genreId, movieId " +
                       "    HAVING COUNT(*) > 1 " +
                       ") " +
                       "SELECT S.genreId, S.movieId, " +
                       "CASE " +
                       "    WHEN S.genreId IS NULL OR S.genreId = '' OR S.genreId NOT REGEXP '^[0-9]+$' THEN 'Invalid or missing genre id' " +
                       "    WHEN S.movieId IS NULL OR S.movieId = '' THEN 'Invalid or missing movie id' " +
                       "    WHEN D.genreId IS NOT NULL AND D.movieId IS NOT NULL THEN 'Duplicate in file' " +
                       "    WHEN G.id IS NULL THEN 'Genre id references nonexistent star' " +
                       "    WHEN M.id IS NULL THEN 'Movie id references nonexistent movie' " +
                       "END AS error " +
                       "FROM genres_in_movies_staging AS S " +
                       "LEFT JOIN dupes AS D ON D.genreId = S.genreId " +
                       "                    AND D.movieId = S.movieId " +
                       "LEFT JOIN genres AS G ON G.id = S.genreId " +
                       "LEFT JOIN movies AS M ON M.id = S.movieId " +
                       "WHERE S.genreId IS NULL OR S.genreId = '' " +
                       "OR S.movieId IS NULL OR S.movieId = '' " +
                       "OR D.genreId IS NOT NULL AND D.movieId IS NOT NULL " +
                       "OR G.id IS NULL " +
                       "OR M.id IS NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            System.out.printf("%s: %s, %s%n",
                              rs.getString("error"),
                              rs.getString("genreId"),
                              rs.getString("movieId")
            );
        }

        rs.close();
        statement.close();
    }

    @Override
    protected void deleteStagingTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS genres_in_movies_staging";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }
}
