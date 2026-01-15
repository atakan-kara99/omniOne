package app.omniOne.chatting.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatsDto(

        UUID conversationId,

        LocalDateTime lastMessageAt,

        UUID otherUserId,

        String otherFirstName,

        String otherLastName

) {}
