package app.omniOne.exception;

public enum ErrorCode {

    VALIDATION_ERROR("Validation Failed"),
    RESOURCE_NOT_FOUND("Resource Not Found"),
    RESOURCE_CONFLICT("Resource Conflict"),
    NOT_ALLOWED("Not Allowed"),
    AUTH_INVALID_CREDENTIALS("Invalid Credentials"),
    AUTH_INVALID_TOKEN("Invalid Token"),
    AUTH_TOKEN_EXPIRED("Token Expired"),
    AUTH_ACCOUNT_DISABLED("Account Disabled"),
    AUTH_REFRESH_INVALID("Refresh Token Invalid"),
    SECURITY_ACCESS_DENIED("Access Denied"),
    SECURITY_CSRF("CSRF Validation Failed"),
    REQUEST_MALFORMED("Malformed Request"),
    REQUEST_METHOD_NOT_ALLOWED("Method Not Allowed"),
    REQUEST_MEDIA_TYPE_UNSUPPORTED("Unsupported Media Type"),
    REQUEST_MISSING_PARAMETER("Missing Parameter"),
    REQUEST_TYPE_MISMATCH("Type Mismatch"),
    INTEGRATION_EMAIL_FAILED("Email Service Failed"),
    INTERNAL_ERROR("Internal Server Error");

    private final String title;

    ErrorCode(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

}
