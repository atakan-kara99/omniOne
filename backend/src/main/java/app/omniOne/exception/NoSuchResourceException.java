package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class NoSuchResourceException extends ApiException {

    public NoSuchResourceException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }

}
