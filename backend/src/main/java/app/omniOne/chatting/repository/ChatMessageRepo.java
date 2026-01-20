package app.omniOne.chatting.repository;

import app.omniOne.chatting.model.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {

    Slice<ChatMessage> findByConversationIdOrderBySentAtDescIdDesc(UUID conversationId, Pageable pageable);

    @Query("""
    SELECT m
    FROM ChatMessage m
    WHERE m.conversation.id = :conversationId
      AND m.sentAt < :beforeSentAt
    ORDER BY m.sentAt DESC, m.id DESC
    """)
    Slice<ChatMessage> findMessagesOlderThan(UUID conversationId, LocalDateTime beforeSentAt, Pageable pageable);

}
