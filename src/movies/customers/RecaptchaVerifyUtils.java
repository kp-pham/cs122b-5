package movies.customers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class RecaptchaVerifyUtils {
    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static void verify(String gRecaptchaResponse) throws Exception {
        URL verifyURL = new URL(SITE_VERIFY_URL);

        HttpsURLConnection conn = (HttpsURLConnection) verifyURL.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String params = "secret=" + System.getenv("SECRET_KEY") + "&response=" + gRecaptchaResponse;

        conn.setDoOutput(true);

        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(params.getBytes());

        outputStream.flush();
        outputStream.close();

        InputStream inputStream = conn.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);

        inputStreamReader.close();

        if (!jsonObject.get("success").getAsBoolean()) {
            throw new Exception("recaptcha verification failed: response was " + jsonObject);
        }
    }
}
