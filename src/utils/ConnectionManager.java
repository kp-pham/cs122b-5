package utils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionManager {
    public static DataSource getMasterDataSource() throws NamingException {
        return (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
    }

    public static DataSource getSlaveDataSource() throws NamingException {
        return (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slave");
    }
}
