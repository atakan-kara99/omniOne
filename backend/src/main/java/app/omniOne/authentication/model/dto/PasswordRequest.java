package app.omniOne.authentication.model.dto;

import app.omniOne.authentication.model.validation.Password;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRequest(

        @NotBlank
        @Size(min = 8, max = 32)
        @Password
        String password

) {

        @Override
        public @Nonnull String toString() {
                return "PasswordRequest[password=***]";
        }

}
