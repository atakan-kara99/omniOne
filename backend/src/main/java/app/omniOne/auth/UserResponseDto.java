package app.omniOne.auth;

import java.time.LocalDate;

public record UserResponseDto(

        Long id,

        String email,

        UserRole role,

        LocalDate createdAt,

        boolean enabled

) {}
