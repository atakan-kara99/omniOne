package app.omniOne.model.dto;

import java.util.UUID;

public record CoachResponse(

        UUID id,

        String firstName,

        String lastName

) {}
