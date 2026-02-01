package app.omniOne.authentication.model.dto;

import app.omniOne.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthResponse(

        UUID id,

        String email,

        UserRole role,

        LocalDateTime createdAt,

        boolean enabled

) {}
