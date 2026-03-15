package app.omniOne.chatting.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageDto(

        Long messageId,

        UUID conversationId,

        UUID senderId,

        LocalDateTime sentAt,

        String content

) {}
