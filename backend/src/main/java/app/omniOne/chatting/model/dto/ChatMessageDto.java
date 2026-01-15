package app.omniOne.chatting.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageDto(

        UUID messageId,

        UUID senderId,

        LocalDateTime sentAt,

        String content

) {}
