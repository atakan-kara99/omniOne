package app.omniOne.exception;

import org.springframework.http.HttpStatus;

public class AccountDisabledException extends ApiException {
    public AccountDisabledException(String message) {
        super(ErrorCode.AUTH_ACCOUNT_DISABLED, HttpStatus.FORBIDDEN, message);
    }
}
