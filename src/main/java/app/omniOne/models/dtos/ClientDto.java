package app.omniOne.models.dtos;

import app.omniOne.models.enums.ClientStatus;

public record ClientDto(Long id, String email, ClientStatus status) {}
