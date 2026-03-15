package app.omniOne.authentication.model.dto;

import app.omniOne.authentication.model.validation.Password;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (

        @NotNull
        @Size(min = 8, max = 32)
        @Password
        String oldPassword,

        @NotNull
        @Size(min = 8, max = 32)
        @Password
        String newPassword

) {

        @Override
        public @Nonnull String toString() {
                return "ChangePasswordRequest[oldPassword=***, newPassword=***]";
        }

}
