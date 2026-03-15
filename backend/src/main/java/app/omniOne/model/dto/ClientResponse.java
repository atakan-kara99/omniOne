package app.omniOne.model.dto;

import java.util.UUID;

public record ClientResponse(

        UUID id,

        String firstName,

        String lastName

) {}
