package app.omniOne.auth.model;

import app.omniOne.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(

   @NotBlank
   @Email
   String email,

   @NotBlank
//   @Size(min = 8, max = 64)
//   @Pattern(regexp = "^\\S+$", message = "Password cannot contain spaces")
//   @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W_]{8,64}$",
//           message = "Password must contain upper, lower, digit, and special character.")
   String password,

   @NotNull
   UserRole role

) {}
