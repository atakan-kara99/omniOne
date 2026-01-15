package app.omniOne.chatting.model.dto;

import java.util.UUID;

public record ChatRequest(

        UUID to,

        String content

) {}
