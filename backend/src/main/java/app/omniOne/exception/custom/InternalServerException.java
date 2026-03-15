package app.omniOne.exception.custom;

import app.omniOne.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InternalServerException extends ApiException {

    public InternalServerException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

}
