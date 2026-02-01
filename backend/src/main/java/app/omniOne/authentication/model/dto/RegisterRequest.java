package app.omniOne.authentication.model.dto;

import app.omniOne.authentication.model.validation.Password;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 32)
        @Password
        String password

) {

        @Override
        public @Nonnull String toString() {
      return "LoginRequest[email=" + email + ", password=***]";
   }

}
