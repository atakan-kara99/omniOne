package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class NotAllowedException extends ApiException {

    public NotAllowedException(String message) {
        super(ErrorCode.NOT_ALLOWED, HttpStatus.FORBIDDEN, message);
    }

}
