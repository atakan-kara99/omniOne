package app.omniOne.models.dtos;

import app.omniOne.models.enums.ClientStatus;

public record ClientResponseDto(

        Long id,

        String email,

        ClientStatus status

) {}
