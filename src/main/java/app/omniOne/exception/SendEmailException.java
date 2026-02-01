package app.omniOne.exception;

public class SendEmailException extends RuntimeException {
    public SendEmailException(String message, Exception ex) {
        super(message, ex);
    }
}
