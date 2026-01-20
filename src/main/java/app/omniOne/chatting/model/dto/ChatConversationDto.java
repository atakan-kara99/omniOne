package app.omniOne.chatting.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatConversationDto(

        UUID conversationId,

        LocalDateTime startedAt,

        LocalDateTime lastMessageAt,

        String lastMessagePreview,

        LocalDateTime lastReadAt,

        UUID otherUserId,

        String otherFirstName,

        String otherLastName

) {}
