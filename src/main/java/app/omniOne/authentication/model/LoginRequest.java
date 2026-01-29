package app.omniOne.authentication.model;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String email,

        @NotBlank
//   @Size(min = 8, max = 64)
//   @Pattern(regexp = "^\\S+$", message = "Password cannot contain spaces")
//   @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W_]{8,64}$",
//           message = "Password must contain upper, lower, digit, and special character.")
        String password

) {

        @Override
        public @Nonnull String toString() {
                return "LoginRequest[username=" + username + ", password=***]";
        }

}
