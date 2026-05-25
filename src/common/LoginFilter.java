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

        String sessionId = getCookieValue(httpRequest, "redisSessionId");

        if (sessionId == null) {
            if (isCustomerOnly(requestURI)) {
                httpResponse.sendRedirect( httpRequest.getContextPath() + "login.html");
            } else {
                httpResponse.sendRedirect( httpRequest.getContextPath() + "/_dashboard/login.html");
            }

            return;
        }

        String sessionKey = "session:" + sessionId;

        try {
            String sessionJson = RedisUtil.get(sessionKey);

            if (sessionJson == null || sessionJson.isEmpty()) {
                if (isCustomerOnly(requestURI)) {
                    httpResponse.sendRedirect( httpRequest.getContextPath() + "login.html");
                } else {
                    httpResponse.sendRedirect( httpRequest.getContextPath() + "/_dashboard/login.html");
                }

                return;
            }

            JsonObject sessionObject = JsonParser.parseString(sessionJson).getAsJsonObject();
            String email = sessionObject.get("email").getAsString();
            String loginTime = sessionObject.get("loginTime").getAsString();
            String userType = sessionObject.get("userType").getAsString();


            if (isCustomerOnly(requestURI)) {
                if (!userType.equals("customer")) {
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
                    return;
                }
            }

            if (isEmployeeOnly(requestURI)) {
                if (!userType.equals("employee")) {
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");
                    return;
                }
            }

            RedisUtil.set(sessionKey, sessionJson, SESSION_TTL_SECONDS);

            httpRequest.setAttribute("email", email);
            httpRequest.setAttribute("loginTime", loginTime);
            httpRequest.setAttribute("userType", userType);

            chain.doFilter(request, response);

        } catch (Exception e) {
            httpRequest.getServletContext().log("Redis error", e);
            httpResponse.sendRedirect("login.html");
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isEmployeeOnly(String requestURI) {
        return requestURI.startsWith("/_dashboard") || requestURI.startsWith("/api/movies.employees");
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

        RedisUtil.init();
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