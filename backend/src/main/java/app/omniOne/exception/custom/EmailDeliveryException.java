package app.omniOne.exception.custom;

import app.omniOne.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class EmailDeliveryException extends ApiException {

    public EmailDeliveryException(String message, Exception ex) {
        super(ErrorCode.INTEGRATION_EMAIL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, message, ex);
    }

}
