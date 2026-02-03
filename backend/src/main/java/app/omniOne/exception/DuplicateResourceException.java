package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, HttpStatus.CONFLICT, message);
    }

}
