package app.omniOne.chatting.repository;

import app.omniOne.chatting.model.dto.ChatsDto;
import app.omniOne.chatting.model.entity.ChatConversation;
import app.omniOne.exception.NoSuchResourceException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    Optional<ChatConversation> findConversationBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("""
    SELECT new app.omniOne.chatting.model.dto.ChatsDto(
        c.id, c.lastMessageAt, u.id, up.firstName, up.lastName)
    FROM ChatConversation c
      JOIN c.participants pMe
      JOIN c.participants pOther
      JOIN pOther.user u
      JOIN u.profile up
    WHERE pMe.user.id = :userId
      AND pOther.user.id <> :userId
    ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC
    """)
    List<ChatsDto> findChats(@Param("userId") UUID userId);

    default ChatConversation findByIdOrThrow(UUID chatId) {
        return findById(chatId)
                .orElseThrow(() -> new NoSuchResourceException("Chat not found"));
    }

}
