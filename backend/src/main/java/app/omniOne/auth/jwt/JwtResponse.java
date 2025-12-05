package app.omniOne.auth.jwt;

import app.omniOne.auth.UserResponseDto;

public record JwtResponse(

        String jwt,

        UserResponseDto user

) {}
