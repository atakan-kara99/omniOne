package app.omniOne.authentication.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,32}$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank())
            return true;
        return PATTERN.matcher(value).matches();
    }

}
