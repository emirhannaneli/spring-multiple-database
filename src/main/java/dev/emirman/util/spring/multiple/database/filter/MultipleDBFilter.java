package dev.emirman.util.spring.multiple.database.filter;


import dev.emirman.util.spring.multiple.database.context.MultipleDBContextHolder;
import dev.emirman.util.spring.multiple.database.exception.DatabaseNotDefined;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@WebFilter(urlPatterns = "/*")
public class MultipleDBFilter implements Filter {
    @Value("${spring.multiple.data.header.name:X-DB-NAME}")
    private String header;
    private final ApplicationContext context;

    public MultipleDBFilter(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest servletRequest) {
            String database = servletRequest.getHeader(header);
            if (database == null) throw new DatabaseNotDefined();
            MultipleDBContextHolder.database(database);
        }
        chain.doFilter(request, response);
    }
}
