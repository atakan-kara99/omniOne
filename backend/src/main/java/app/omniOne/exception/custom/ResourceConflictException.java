package app.omniOne.exception.custom;

import app.omniOne.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceConflictException extends ApiException {

    public ResourceConflictException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, HttpStatus.CONFLICT, message);
    }

}
