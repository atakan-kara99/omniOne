package app.omniOne.exception.custom;

import app.omniOne.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtInvalidException extends ApiException {
    public JwtInvalidException(String message) {
        super(ErrorCode.AUTH_INVALID_TOKEN, HttpStatus.BAD_REQUEST, message);
    }
}
