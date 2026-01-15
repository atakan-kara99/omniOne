package app.omniOne.chatting.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatDto(

        UUID conversationId,

        LocalDateTime createdAt,

        List<ChatMessageDto> messages

) {}
