package app.omniOne.authentication.model.dto;

import app.omniOne.authentication.model.validation.Password;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (

        @NotBlank
        @Size(min = 8, max = 32)
        @Password
        String oldPassword,

        @NotBlank
        @Size(min = 8, max = 32)
        @Password
        String newPassword

) {

        @Override
        public @Nonnull String toString() {
                return "ChangePasswordRequest[oldPassword=***, newPassword=***]";
        }

}
