package app.omniOne.exception;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfToken;
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
            String headerToken = request.getHeader("X-XSRF-TOKEN");
            Cookie[] cookies = request.getCookies();
            String cookieNames = cookies == null ? "none"
                    : Arrays.stream(cookies).map(Cookie::getName).collect(Collectors.joining(","));
            String cookieToken = findCookieValue("XSRF-TOKEN", cookies);
            CsrfToken requestToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            String requestTokenValue = requestToken == null ? null : requestToken.getToken();
            log.warn("CSRF denied: method={} path={} origin={} referer={} headerToken={} cookieToken={} requestToken={} headerEqCookie={} headerEqRequest={} cookies={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getHeader("Origin"),
                    request.getHeader("Referer"),
                    maskToken(headerToken),
                    maskToken(cookieToken),
                    maskToken(requestTokenValue),
                    tokensEqual(headerToken, cookieToken),
                    tokensEqual(headerToken, requestTokenValue),
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

    private String findCookieValue(String name, Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean tokensEqual(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        int len = token.length();
        if (len <= 8) {
            return token + "(len=" + len + ")";
        }
        return token.substring(0, 4) + "...(len=" + len + ")";
    }

}
