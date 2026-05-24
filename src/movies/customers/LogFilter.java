package movies.customers;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

@WebFilter(filterName = "movies.customers.LogFilter", urlPatterns="/api/movies.customers/search")
public class LogFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        long start = System.nanoTime();

        chain.doFilter(request, response);

        long end = System.nanoTime();

        long ts = end - start;
        long tj = (long) request.getAttribute("TJ");

        LogWriter.log(ts, tj);
    }
}
