package common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns="/*")
public class LoginFilter implements Filter {
    private static final int SESSION_TTL_SECONDS = 24 * 60 * 60;
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        if (this.isUrlAllowedWithoutLogin(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        if (isEmployeeOnly(requestURI)) {
            if (httpRequest.getSession().getAttribute("employee") == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        if (isCustomerOnly(requestURI)) {
            if (httpRequest.getSession().getAttribute("customer") == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isEmployeeOnly(String requestURI) {
        return requestURI.startsWith("/_dashboard") || requestURI.startsWith("/api/employees");
    }

    private boolean isCustomerOnly(String requestURI) {
        return requestURI.startsWith("/");
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("_dashboard/login.html");
        allowedURIs.add("api/customers/login");
        allowedURIs.add("api/employees/login");

        allowedURIs.add(".css");
        allowedURIs.add(".ico");
        allowedURIs.add(".png");
    }

    private void validateSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        String sessionId = getCookieValue(httpRequest, "redisSessionId");

        if (sessionId == null) {
            httpResponse.sendRedirect("login.html");
            return;
        }

        String sessionKey = "session:" + sessionId;
        try {
            String sessionJson = RedisUtil.get(sessionKey);

            if (sessionJson == null || sessionJson.isEmpty()) {
                httpResponse.sendRedirect("login.html");
                return;
            }

            JsonObject sessionObject = JsonParser.parseString(sessionJson).getAsJsonObject();
            String username = sessionObject.get("username").getAsString();
            String loginTime = sessionObject.get("loginTime").getAsString();

            RedisUtil.set(sessionKey, loginTime, SESSION_TTL_SECONDS);

            httpRequest.setAttribute("username", username);
            httpRequest.setAttribute("loginTime", loginTime);

            chain.doFilter(request, response);
        } catch (Exception e) {
            httpRequest.getServletContext().log("Redis error", e);
            httpResponse.sendRedirect("login.html");
        }
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}