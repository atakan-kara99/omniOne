package app.omniOne.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ProblemDetailFactory problemDetailFactory;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        ErrorCode errorCode = ErrorCode.AUTH_INVALID_CREDENTIALS;
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String detail = "Authentication is required";
        if (ex instanceof DisabledException) {
            errorCode = ErrorCode.AUTH_ACCOUNT_DISABLED;
            status = HttpStatus.FORBIDDEN;
            detail = "Account is disabled";
        } else if (ex instanceof BadCredentialsException) {
            errorCode = ErrorCode.AUTH_INVALID_CREDENTIALS;
            detail = "Invalid credentials";
        }
        log.warn("Authentication failed");
        problemDetailFactory.write(
                request,
                response,
                status,
                errorCode,
                errorCode.title(),
                detail,
                Map.of());
    }

}
