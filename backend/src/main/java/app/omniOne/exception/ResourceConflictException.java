package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends ApiException {

    public ResourceConflictException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, HttpStatus.CONFLICT, message);
    }

}
