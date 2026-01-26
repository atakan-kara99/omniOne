package app.omniOne.authentication.model;

public record LoginResponse(

        String jwt,

        String refreshToken

) {}
