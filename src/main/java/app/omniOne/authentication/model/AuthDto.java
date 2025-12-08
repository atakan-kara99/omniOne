package app.omniOne.authentication.model;

import app.omniOne.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthDto(

        UUID id,

        String email,

        UserRole role,

        LocalDateTime createdAt,

        boolean enabled

) {}
