package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class JwtExpiredException extends ApiException {
    public JwtExpiredException(String message) {
        super(ErrorCode.AUTH_TOKEN_EXPIRED, HttpStatus.BAD_REQUEST, message);
    }
}
