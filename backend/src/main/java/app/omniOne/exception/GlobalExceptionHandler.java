package app.omniOne.exception;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ProblemDetail> handleDisabled(DisabledException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail pd = pd("User Account Disabled", status, ex.getMessage());
        log.warn("Failed to login to account because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(SendEmailException.class)
    public ResponseEntity<ProblemDetail> handleSendMail(SendEmailException ex) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        ProblemDetail pd = pd("Send Email Failed", status, ex.getMessage());
        log.error("Failed to process or send email", ex);
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(JWTDecodeException.class)
    public ResponseEntity<ProblemDetail> handleJWTDecode(JWTDecodeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = pd("JWT Decode", status, ex.getMessage());
        log.info("Failed to decode provided JWT because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ProblemDetail> handleJWTExpiry(TokenExpiredException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail pd = pd("JWT Expired", status, ex.getMessage());
        log.info("Failed to validate provided JWT because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ProblemDetail> handleNoAuthorization(AuthorizationDeniedException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail pd = pd("Authorization Denied", status, ex.getMessage());
        log.info("Failed to authorize to the server because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemDetail pd = pd("No Resource Found", status, ex.getMessage());
        log.info("Failed to find resource because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        ProblemDetail pd = pd("Http Request Method Not Supported", status, ex.getMessage());
        log.info("Failed to process http request because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCred(BadCredentialsException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail pd = pd("Bad Credentials", status, ex.getMessage());
        log.info("Failed to authorize request because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleParsing(HttpMessageNotReadableException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = pd("Http Message Not Readable", status, "Invalid request body");
        log.info("Failed to read http message because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = pd("Validation Failed", status);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        pd.setProperty("errors", errors);
        log.info("Failed to validate http request because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleMethodValidation(HandlerMethodValidationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = pd("Validation Failed", status);
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe
                    ? fe.getField()
                    : "request";
            errors.put(field, error.getDefaultMessage());
        });
        pd.setProperty("errors", errors);
        log.info("Failed to validate method because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(NotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleNotAllowed(NotAllowedException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail pd = pd("Not Allowed", status, ex.getMessage());
        log.info("Failed to process request because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateResource(DuplicateResourceException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail pd = pd("Duplicate Resource", status, ex.getMessage());
        log.info("Failed to insert into database because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(NoSuchResourceException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchResource(NoSuchResourceException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemDetail pd = pd("No Such Resource", status, ex.getMessage());
        log.info("Failed to retrieve from database because: {}", ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail pd = pd("Something went wrong", status);
        log.error("Failed to exist", ex);
        return new ResponseEntity<>(pd, status);
    }

    // Helper Functions
    private ProblemDetail pd(String title, HttpStatus status) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setTitle(title);
        return pd;
    }

    private ProblemDetail pd(String title, HttpStatus status, String message) {
        ProblemDetail pd = pd(title, status);
        pd.setDetail(message);
        return pd;
    }

}
