package app.omniOne.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientPostDto(

        @NotBlank
        @Email
        String email

) {}
