package dev.emirman.util.spring.multiple.database.filter;


import dev.emirman.util.spring.multiple.database.context.MultipleDBContextHolder;
import dev.emirman.util.spring.multiple.database.exception.DatabaseNotDefined;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;

@Configuration
@WebFilter(urlPatterns = "/*")
@Import({MultipleDBContextHolder.class})
public class MultipleDBFilter implements Filter {
    @Value("${spring.multiple.database.header.name:X-Data-Source}")
    private String header;

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
