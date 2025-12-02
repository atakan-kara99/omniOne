package app.omniOne.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CoachPatchDto(

        @NotBlank
        @Email
        String email

) {}
