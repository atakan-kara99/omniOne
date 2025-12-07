package app.omniOne.model.dto;

import app.omniOne.model.enums.Gender;

import java.time.LocalDate;

public record UserProfileDto (

        String firstName,

        String lastName,

        LocalDate birthDate,

        Gender gender

) {}
