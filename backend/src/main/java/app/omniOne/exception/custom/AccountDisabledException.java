package app.omniOne.exception.custom;

import app.omniOne.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AccountDisabledException extends ApiException {
    public AccountDisabledException(String message) {
        super(ErrorCode.AUTH_ACCOUNT_DISABLED, HttpStatus.FORBIDDEN, message);
    }
}
