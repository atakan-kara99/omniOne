package app.omniOne.authentication.model.dto;

import jakarta.annotation.Nonnull;

public record LoginResponse(

        String jwt,

        String refreshToken

) {

    @Override
    public @Nonnull String toString() {
        return "LoginResponse[jwt=" + jwt + ", refreshToken=***]";
    }

}
