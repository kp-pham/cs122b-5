package loaders;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GenreLoader extends DataLoader {
    public GenreLoader(Connection conn) { super(conn); }

    @Override
    protected void createStagingTable() throws SQLException {
        String dropQuery = "DROP TABLE IF EXISTS genres_staging";
        String createQuery = "CREATE TABLE genres_staging( " +
                             "    id TEXT, " +
                             "    name TEXT " +
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
                       "INTO TABLE genres_staging " +
                       "FIELDS TERMINATED BY ',' " +
                       "ENCLOSED BY '\"' " +
                       "LINES TERMINATED BY '\\n' " +
                       "IGNORE 1 ROWS " +
                       "(@id, @name) " +
                       "SET " +
                       "    id = TRIM(@id), " +
                       "    name = TRIM(@name)";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, file);

        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void validateAndTransform() throws SQLException {
        String query = "INSERT INTO genres (id, name) " +
                       "WITH deduped AS ( " +
                       "    SELECT id " +
                       "    FROM genres_staging " +
                       "    GROUP BY id " +
                       "    HAVING COUNT(*) = 1 " +
                       "), " +
                       "cleaned AS ( " +
                       "    SELECT CAST(S.id AS UNSIGNED) AS id, S.name " +
                       "    FROM genres_staging AS S " +
                       "    INNER JOIN deduped AS D ON D.id = S.id " +
                       "    WHERE S.id IS NOT NULL AND S.id != '' AND S.id REGEXP '^[0-9]+$' " +
                       "    AND S.name IS NOT NULL AND S.name != '' " +
                       ") " +
                       "SELECT C.id, C.name " +
                       "FROM cleaned AS C " +
                       "LEFT JOIN genres AS G ON G.id = C.id " +
                       "WHERE G.id IS NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void reportErrors() throws SQLException {
        String query = "WITH dupes AS ( " +
                       "    SELECT id " +
                       "    FROM genres_staging " +
                       "    GROUP BY id " +
                       "    HAVING COUNT(*) > 1 " +
                       ") " +
                       "SELECT S.id, S.name, " +
                       "CASE " +
                       "    WHEN S.id IS NULL OR S.id = '' OR S.id NOT REGEXP '^[0-9]+$' THEN 'Invalid or missing id' " +
                       "    WHEN S.name IS NULL OR S.name = '' THEN 'Invalid or missing name' " +
                       "    WHEN D.id IS NOT NULL THEN 'Duplicate in file' " +
                       "    WHEN G.id IS NOT NULl THEN 'Genre already exists in database' " +
                       "END AS error " +
                       "FROM genres_staging AS S " +
                       "LEFT JOIN dupes AS D ON D.id = S.id " +
                       "LEFT JOIN genres AS G ON G.id = S.id " +
                       "WHERE S.id IS NULL OR S.id = '' OR S.id NOT REGEXP '^[0-9]+$' " +
                       "OR S.name IS NULL OR S.name = '' " +
                       "OR D.id IS NOT NULL " +
                       "OR G.id IS NOT NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            System.out.printf("%s: %s, %s%n",
                              rs.getString("error"),
                              rs.getString("id"),
                              rs.getString("name")
            );
        }

        rs.close();
        statement.close();
    }

    @Override
    protected void deleteStagingTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS genres_staging";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }
}
