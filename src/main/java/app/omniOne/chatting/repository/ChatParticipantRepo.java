package app.omniOne.chatting.repository;

import app.omniOne.chatting.model.entity.ChatParticipant;
import app.omniOne.chatting.model.entity.ChatParticipantId;
import app.omniOne.exception.NoSuchResourceException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatParticipantRepo extends JpaRepository<ChatParticipant, ChatParticipantId> {

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    default ChatParticipant findByIdOrThrow(ChatParticipantId participantId) {
        return findById(participantId).orElseThrow(() -> new NoSuchResourceException("ChatParticipant not found"));
    }

}
