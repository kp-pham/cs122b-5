import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns="/*")
public class LoginFilter implements Filter {
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
}