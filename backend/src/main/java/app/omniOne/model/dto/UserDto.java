package app.omniOne.model.dto;

import app.omniOne.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto (

        UUID id,

        String email,

        UserRole role,

        LocalDateTime updatedAt

) {}
