package movies.customers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {
    private static final String LOGFILE_PATH = System.getProperty("LOGFILE_PATH");

    public static synchronized void log(long ts, long tj) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOGFILE_PATH, true))) {
            writer.write("TS=" + ts + ",TJ=" + tj);
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
