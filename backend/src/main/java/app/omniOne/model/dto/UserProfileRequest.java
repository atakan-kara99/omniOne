package app.omniOne.model.dto;

import app.omniOne.model.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserProfileRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotNull
        LocalDate birthDate,

        @NotNull
        Gender gender,

        @NotBlank
        @Size(min = 2, max = 2)
        String countryCode,

        @NotBlank
        @Size(max = 255)
        String city

) {}
