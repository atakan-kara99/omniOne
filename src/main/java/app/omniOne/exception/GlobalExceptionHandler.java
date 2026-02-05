package app.omniOne.exception;

import app.omniOne.exception.custom.AccountDisabledException;
import app.omniOne.exception.custom.ApiException;
import app.omniOne.exception.custom.JwtExpiredException;
import app.omniOne.exception.custom.JwtInvalidException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ProblemDetailFactory problemDetailFactory;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(ApiException ex, HttpServletRequest request) {
        ProblemDetail pd = problemDetailFactory.create(
                request,
                ex.getStatus(),
                ex.getErrorCode(),
                ex.getErrorCode().title(),
                ex.getMessage(),
                ex.getDetails());
        if (ex.getStatus().is5xxServerError()) {
            log.error("Request failed (code={}, status={})",
                    ex.getErrorCode(), ex.getStatus(), ex);
        } else {
            log.warn("Request failed (code={}, status={})",
                    ex.getErrorCode(), ex.getStatus());
        }
        return new ResponseEntity<>(pd, ex.getStatus());
    }

    @ExceptionHandler(JWTDecodeException.class)
    public ResponseEntity<ProblemDetail> handleJwtDecode(JWTDecodeException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.AUTH_INVALID_TOKEN,
                ErrorCode.AUTH_INVALID_TOKEN.title(),
                "Token is invalid",
                Map.of());
        log.warn("JWT decode failed");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(JwtExpiredException.class)
    public ResponseEntity<ProblemDetail> handleJwtExpired(JwtExpiredException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.AUTH_TOKEN_EXPIRED,
                ErrorCode.AUTH_TOKEN_EXPIRED.title(),
                ex.getMessage(),
                Map.of());
        log.warn("JWT expired");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(JwtInvalidException.class)
    public ResponseEntity<ProblemDetail> handleJwtInvalid(JwtInvalidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.AUTH_INVALID_TOKEN,
                ErrorCode.AUTH_INVALID_TOKEN.title(),
                ex.getMessage(),
                Map.of());
        log.warn("JWT invalid");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ProblemDetail> handleAccountDisabled(AccountDisabledException ex,
                                                               HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.AUTH_ACCOUNT_DISABLED,
                ErrorCode.AUTH_ACCOUNT_DISABLED.title(),
                ex.getMessage(),
                Map.of());
        log.warn("Account disabled");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.AUTH_INVALID_CREDENTIALS,
                ErrorCode.AUTH_INVALID_CREDENTIALS.title(),
                "Invalid credentials",
                Map.of());
        log.warn("Bad credentials");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAuthorizationDenied(AuthorizationDeniedException ex,
                                                                   HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.SECURITY_ACCESS_DENIED,
                ErrorCode.SECURITY_ACCESS_DENIED.title(),
                "Access is denied",
                Map.of());
        log.warn("Authorization denied");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex,
                                                               HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND.title(),
                "Resource not found",
                Map.of());
        log.warn("No resource found");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest request) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.REQUEST_METHOD_NOT_ALLOWED,
                ErrorCode.REQUEST_METHOD_NOT_ALLOWED.title(),
                "Method not allowed",
                Map.of("method", ex.getMethod()));
        log.warn("Method not supported");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.REQUEST_MEDIA_TYPE_UNSUPPORTED,
                ErrorCode.REQUEST_MEDIA_TYPE_UNSUPPORTED.title(),
                "Unsupported media type",
                Map.of("contentType", ex.getContentType() == null ? "unknown" : ex.getContentType().toString()));
        log.warn("Unsupported media type");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.REQUEST_MISSING_PARAMETER,
                ErrorCode.REQUEST_MISSING_PARAMETER.title(),
                "Missing required parameter",
                Map.of("parameter", ex.getParameterName()));
        log.warn("Missing parameter");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.REQUEST_TYPE_MISMATCH,
                ErrorCode.REQUEST_TYPE_MISMATCH.title(),
                "Parameter has invalid value",
                Map.of("parameter", ex.getName(), "value", String.valueOf(ex.getValue())));
        log.warn("Type mismatch");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleParsing(HttpMessageNotReadableException ex,
                                                       HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.REQUEST_MALFORMED,
                ErrorCode.REQUEST_MALFORMED.title(),
                "Invalid request body",
                Map.of());
        log.warn("Unreadable request body");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, List<String>> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                addError(errors, error.getField(), error.getDefaultMessage()));
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.title(),
                "Validation failed",
                Map.of("errors", errors));
        log.warn("Validation failed");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleMethodValidation(HandlerMethodValidationException ex,
                                                                HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, List<String>> errors = new LinkedHashMap<>();
        ex.getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : "request";
            addError(errors, field, error.getDefaultMessage());
        });
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.title(),
                "Validation failed",
                Map.of("errors", errors));
        log.warn("Method validation failed");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, List<String>> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                addError(errors, String.valueOf(violation.getPropertyPath()), violation.getMessage()));
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.title(),
                "Validation failed",
                Map.of("errors", errors));
        log.warn("Constraint violation");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex,
                                                            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.RESOURCE_CONFLICT,
                ErrorCode.RESOURCE_CONFLICT.title(),
                "Request conflicts with existing data",
                Map.of());
        log.warn("Data integrity violation");
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail pd = problemDetailFactory.create(
                request,
                status,
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.title(),
                "Unexpected server error",
                Map.of());
        log.error("Unexpected server error", ex);
        return new ResponseEntity<>(pd, status);
    }

    private void addError(Map<String, List<String>> errors, String field, String message) {
        errors.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
    }

}
