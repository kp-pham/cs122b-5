package loaders;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StarLoader extends DataLoader {
    public StarLoader(Connection conn) {
        super(conn);
    }

    @Override
    protected void createStagingTable() throws SQLException {
        String dropQuery = "DROP TABLE IF EXISTS stars_staging";
        String createQuery = "CREATE TABLE stars_staging( " +
                             "    id TEXT, " +
                             "    name TEXT, " +
                             "    birthYear TEXT " +
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
                       "INTO TABLE stars_staging " +
                       "FIELDS TERMINATED BY ',' " +
                       "ENCLOSED BY '\"' " +
                       "LINES TERMINATED BY '\\n' " +
                       "IGNORE 1 ROWS " +
                       "(@id, @name, @birthYear) " +
                       "SET " +
                       "    id = TRIM(@id), " +
                       "    name = TRIM(@name), " +
                       "    birthYear = TRIM(@birthYear)";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, file);

        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void validateAndTransform() throws SQLException {
        String query = "INSERT INTO stars (id, name, birthYear) " +
                       "WITH deduped AS ( " +
                       "    SELECT id " +
                       "    FROM stars_staging " +
                       "    GROUP BY id " +
                       "    HAVING COUNT(*) = 1 " +
                       "), " +
                       "cleaned AS ( " +
                       "    SELECT S.id, S.name, " +
                       "    CASE " +
                       "        WHEN S.birthyear IS NOT NULL AND S.birthYear != '' AND S.birthYear REGEXP '^[0-9]+$' THEN CAST(S.birthYear AS UNSIGNED) " +
                       "        ELSE NULL " +
                       "    END AS birthYear " +
                       "    FROM stars_staging AS S " +
                       "    INNER JOIN deduped AS D ON D.id = S.id " +
                       "    WHERE S.id IS NOT NULL AND S.id != '' " +
                       "    AND S.name IS NOT NULL AND S.name != '' " +
                       ") " +
                       "SELECT C.id, C.name, C.birthYear " +
                       "FROM cleaned AS C " +
                       "LEFT JOIN stars AS S ON C.id = S.id " +
                       "WHERE S.id IS NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    protected void reportErrors() throws SQLException {
        String query = "WITH dupes AS ( " +
                       "    SELECT id " +
                       "    FROM stars_staging " +
                       "    GROUP BY id " +
                       "    HAVING COUNT(*) > 1 " +
                       ") " +
                       "SELECT S.id, S.name, S.birthYear, " +
                       "CASE " +
                       "    WHEN S.id IS NULL OR S.id = '' THEN 'Invalid or missing id' " +
                       "    WHEN S.name IS NULL OR S.name = '' THEN 'Invalid or missing name' " +
                       "    WHEN S.birthYear IS NOT NULL AND S.birthYear != '' AND S.birthYear NOT REGEXP '^[0-9]+$' THEN 'Invalid birth year' " +
                       "    WHEN D.id IS NOT NULL THEN 'Duplicate in file' " +
                       "    WHEN stars.id IS NOT NULL THEN 'Star already exists in database' " +
                       "END AS error " +
                       "FROM stars_staging AS S " +
                       "LEFT JOIN dupes AS D ON D.id = S.id " +
                       "LEFT JOIN stars ON stars.id = S.id " +
                       "WHERE S.id IS NULL OR S.id = '' " +
                       "OR S.name IS NULL OR S.name = ''" +
                       "OR (S.birthYear IS NOT NULL AND S.birthYear != '' AND S.birthYear NOT REGEXP '^[0-9]+$')" +
                       "OR D.id IS NOT NULL " +
                       "OR stars.id IS NOT NULL";

        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            System.out.printf("%s: %s, %s, %s%n",
                              rs.getString("error"),
                              rs.getString("id"),
                              rs.getString("name"),
                              rs.getString("birthYear")

            );
        }

        rs.close();
        statement.close();
    }

    @Override
    protected void deleteStagingTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS stars_staging";

        PreparedStatement statement = conn.prepareStatement(query);
        statement.executeUpdate();
        statement.close();
    }
}
