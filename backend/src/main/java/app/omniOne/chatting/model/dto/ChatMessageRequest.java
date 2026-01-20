package app.omniOne.chatting.model.dto;

import java.util.UUID;

public record ChatMessageRequest(

        UUID to,

        String content

) {}
