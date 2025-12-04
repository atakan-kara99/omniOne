package app.omniOne.model.dto;

import app.omniOne.model.enums.ClientStatus;

public record ClientResponseDto(

        Long id,

        String email,

        ClientStatus status

) {}
