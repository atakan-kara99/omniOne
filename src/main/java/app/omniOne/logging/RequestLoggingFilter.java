package app.omniOne.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId + "HT");

        String method = request.getMethod();
        String path = request.getRequestURI();
        if (!method.equals("OPTIONS"))
            log.info("→ {} {}", method, path);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("Request failed: {} {}", method, path);
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            HttpStatus status = HttpStatus.valueOf(response.getStatus());
            if (!method.equals("OPTIONS"))
                log.info("← {} {} with {} in {} ms", method, path, status, duration);
            MDC.clear();
        }
    }

}
