package app.omniOne.authentication.model;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 32)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,32}$",
                message = "Password must be 8–32 characters and include at least one uppercase letter, " +
                        "one lowercase letter, and one number. Special characters are allowed. No spaces.")
        String password

) {

        @Override
        public @Nonnull String toString() {
      return "LoginRequest[email=" + email + ", password=***]";
   }

}
