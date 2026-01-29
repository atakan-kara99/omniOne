package app.omniOne.chatting.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageAckDto(

        UUID clientMessageId,

        Long messageId,

        UUID conversationId,

        LocalDateTime sentAt

) {}
