package app.omniOne.auth;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDto(

        UUID id,

        String email,

        UserRole role,

        LocalDate createdAt,

        boolean enabled

) {}
