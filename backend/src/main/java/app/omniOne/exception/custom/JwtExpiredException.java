package app.omniOne.exception.custom;

import app.omniOne.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtExpiredException extends ApiException {
    public JwtExpiredException(String message) {
        super(ErrorCode.AUTH_TOKEN_EXPIRED, HttpStatus.BAD_REQUEST, message);
    }
}
