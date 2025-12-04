package app.omniOne.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientPatchDto(

        @NotBlank
        @Email
        String email

) {}
