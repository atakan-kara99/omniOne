package app.omniOne.chatting.repository;

import app.omniOne.chatting.model.dto.ChatConversationDto;
import app.omniOne.chatting.model.entity.ChatConversation;
import app.omniOne.exception.custom.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatConversationRepo extends JpaRepository<ChatConversation, UUID> {

    @Query("""
    SELECT c
    FROM ChatConversation c
      JOIN c.participants p1
      JOIN c.participants p2
    WHERE p1.user.id = :userA
      AND p2.user.id = :userB
    """)
    Optional<ChatConversation> findConversationBetween(UUID userA, UUID userB);

    @Query("""
    SELECT new app.omniOne.chatting.model.dto.ChatConversationDto(
        c.id, c.startedAt, c.lastMessageAt, c.lastMessagePreview, pMe.lastReadAt, u.id, up.firstName, up.lastName)
    FROM ChatConversation c
      JOIN c.participants pMe
      JOIN c.participants pOther
      JOIN pOther.user u
      JOIN u.profile up
    WHERE pMe.user.id = :userId
      AND pOther.user.id <> :userId
    """)
    List<ChatConversationDto> findConversationsOf(UUID userId);

    default ChatConversation findByIdOrThrow(UUID chatId) {
        return findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
    }

}
