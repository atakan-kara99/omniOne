package app.omniOne.authentication.model.dto;

public record InvitationResponse(

        String email,

        boolean requiresPassword

) {}
