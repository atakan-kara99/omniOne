package app.omniOne.email.properties;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailPropsValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void activationProps_invalidValuesHaveViolations() {
        ActivationProps props = new ActivationProps("", "", "", 0);
        assertEquals(4, validator.validate(props).size());
    }

    @Test
    void invitationProps_invalidValuesHaveViolations() {
        InvitationProps props = new InvitationProps("", "", "", -1);
        assertEquals(4, validator.validate(props).size());
    }

    @Test
    void resetPasswordProps_validValuesHaveNoViolations() {
        ResetPasswordProps props = new ResetPasswordProps("/reset", "email/reset_password.html", "Reset", 15);
        assertTrue(validator.validate(props).isEmpty());
    }
}
