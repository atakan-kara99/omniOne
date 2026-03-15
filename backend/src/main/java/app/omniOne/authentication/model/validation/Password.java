package app.omniOne.authentication.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {

    String message() default "Password must be 8–32 characters and include at least one uppercase letter, "
            + "one lowercase letter, and one number. Special characters are allowed. No spaces.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
