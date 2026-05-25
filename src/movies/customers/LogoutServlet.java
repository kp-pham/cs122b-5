package movies.customers;

import common.RedisUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "customers.LogoutServlet", urlPatterns = "/api/customers/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionId = null;
        RedisUtil.getCookieValue(request, "sessionRedisId");

        if (sessionId != null) {
            RedisUtil.deleteSession(sessionId);
        }

        response.sendRedirect(request.getContextPath() + "/login.html");
    }
}
