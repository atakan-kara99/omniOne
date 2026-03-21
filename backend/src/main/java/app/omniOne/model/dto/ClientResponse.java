package app.omniOne.model.dto;

import app.omniOne.model.enums.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record ClientResponse(

        UUID id,

        String firstName,

        String lastName,

        LocalDate birthDate,

        Gender gender,

        String countryCode,

        String city

) {}
