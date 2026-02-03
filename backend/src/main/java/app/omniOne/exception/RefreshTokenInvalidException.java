package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenInvalidException extends ApiException {

    public RefreshTokenInvalidException(String message) {
        super(ErrorCode.AUTH_REFRESH_INVALID, HttpStatus.UNAUTHORIZED, message);
    }

}
