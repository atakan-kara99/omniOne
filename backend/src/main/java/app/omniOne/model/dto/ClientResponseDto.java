package app.omniOne.model.dto;

import app.omniOne.model.enums.ClientStatus;

import java.util.UUID;

public record ClientResponseDto(

        UUID id,

        String email,

        ClientStatus status

) {}
