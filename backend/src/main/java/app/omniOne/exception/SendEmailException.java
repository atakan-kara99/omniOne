package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class SendEmailException extends ApiException {

    public SendEmailException(String message, Exception ex) {
        super(ErrorCode.INTEGRATION_EMAIL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, message, ex);
    }

}
