package app.omniOne.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCred(BadCredentialsException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail pd = pd("Bad Credentials", status, ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleParsing(HttpMessageNotReadableException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = pd("Http Message Not Readable", status, ex.getMessage());
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
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(AccessForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleAccessForbidden(AccessForbiddenException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail pd = pd("Access Forbidden", status, ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateResource(DuplicateResourceException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail pd = pd("Duplicate Resource", status, ex.getMessage());
        return new ResponseEntity<>(pd, status);
    }

    @ExceptionHandler(NoSuchResourceException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchResource(NoSuchResourceException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemDetail pd = pd("No Such Resource", status, ex.getMessage());
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
