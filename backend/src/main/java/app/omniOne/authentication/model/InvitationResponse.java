package app.omniOne.authentication.model;

public record InvitationResponse(

        String email,

        boolean requiresPassword

) {}
