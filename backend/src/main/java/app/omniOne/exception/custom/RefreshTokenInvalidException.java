package app.omniOne.exception.custom;

import app.omniOne.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class RefreshTokenInvalidException extends ApiException {

    public RefreshTokenInvalidException(String message) {
        super(ErrorCode.AUTH_REFRESH_INVALID, HttpStatus.UNAUTHORIZED, message);
    }

}
