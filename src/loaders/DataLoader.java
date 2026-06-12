package loaders;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataLoader {
    protected final Connection conn;

    public DataLoader(Connection conn) {
        this.conn = conn;
    }

    void load(String file) throws Exception {
        try {
            conn.setAutoCommit(false);

            createStagingTable();
            System.out.println("Created staging table.");

            loadToStaging(file);
            System.out.println("Loaded data to staging table.");

            validateAndTransform();
            System.out.println("Loaded data to database.\n");

            System.out.println("Errors reported: ");
            reportErrors();

            conn.commit();

        } catch (Exception e) {
            conn.rollback();
            throw e;

        } finally {
            deleteStagingTable();
            conn.setAutoCommit(true);
        }
    }

    protected abstract void createStagingTable() throws SQLException;
    protected abstract void loadToStaging(String file) throws SQLException;
    protected abstract void validateAndTransform() throws SQLException;
    protected abstract void reportErrors() throws SQLException;
    protected abstract void deleteStagingTable() throws SQLException;
}
