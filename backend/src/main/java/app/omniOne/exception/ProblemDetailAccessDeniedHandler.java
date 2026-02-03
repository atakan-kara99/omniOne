package app.omniOne.exception;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailFactory problemDetailFactory;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        ErrorCode errorCode = ErrorCode.SECURITY_ACCESS_DENIED;
        String detail = "Access is denied";
        if (ex instanceof CsrfException) {
            errorCode = ErrorCode.SECURITY_CSRF;
            detail = "CSRF token is missing or invalid";
            String headerToken = request.getHeader("X-CSRF-TOKEN");
            String cookieNames = request.getCookies() == null ? "none"
                    : Arrays.stream(request.getCookies())
                    .map(Cookie::getName)
                    .collect(Collectors.joining(","));
            log.warn("CSRF denied: method={} path={} origin={} referer={} headerTokenPresent={} cookies={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getHeader("Origin"),
                    request.getHeader("Referer"),
                    headerToken != null,
                    cookieNames);
        } else {
            log.warn("Access denied");
        }
        problemDetailFactory.write(
                request,
                response,
                org.springframework.http.HttpStatus.FORBIDDEN,
                errorCode,
                errorCode.title(),
                detail,
                Map.of());
    }

}
