package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class OperationNotAllowedException extends ApiException {

    public OperationNotAllowedException(String message) {
        super(ErrorCode.NOT_ALLOWED, HttpStatus.FORBIDDEN, message);
    }

}
