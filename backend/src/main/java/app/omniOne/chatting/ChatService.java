package app.omniOne.chatting;

import app.omniOne.chatting.model.ChatMapper;
import app.omniOne.chatting.model.dto.ChatConversationDto;
import app.omniOne.chatting.model.dto.ChatMessageDto;
import app.omniOne.chatting.model.entity.ChatConversation;
import app.omniOne.chatting.model.entity.ChatMessage;
import app.omniOne.chatting.model.entity.ChatParticipant;
import app.omniOne.chatting.model.entity.ChatParticipantId;
import app.omniOne.chatting.repository.ChatConversationRepo;
import app.omniOne.chatting.repository.ChatMessageRepo;
import app.omniOne.chatting.repository.ChatParticipantRepo;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepo userRepo;
    private final ChatMapper chatMapper;
    private final ChatMessageRepo messageRepo;
    private final ChatParticipantRepo participantRepo;
    private final ChatConversationRepo conversationRepo;

    public List<ChatConversationDto> getChatConversations(UUID userId) {
        log.debug("Trying to retrieve ChatConversations from User {}", userId);
        List<ChatConversationDto> chats = conversationRepo.findConversationsOf(userId);
        log.info("Successfully retrieved ChatConversations");
        return chats;
    }

    public Slice<ChatMessage> getSliceOfMessages(UUID conversationId, LocalDateTime beforeSentAt, int size) {
        log.debug("Trying to retrieve slice of {} ChatMessages of ChatConversation {} before {}"
                , size, conversationId, beforeSentAt);
        Pageable pageable = PageRequest.of(0, size);
        Slice<ChatMessage> chatMessages;
        if (beforeSentAt == null) {
            chatMessages = messageRepo.findByConversationIdOrderBySentAtDescIdDesc(conversationId, pageable);
        } else {
            chatMessages = messageRepo.findMessagesOlderThan(conversationId, beforeSentAt, pageable);
        }
        log.info("Successfully retrieve slice of ChatMessages");
        return chatMessages;
    }

    @Transactional
    public ChatMessageDto saveMessage(UUID fromId, UUID toId, String content) {
        log.debug("Trying to save ChatMessage from User {} to User {}", fromId, toId);
        LocalDateTime now = LocalDateTime.now();
        ChatConversation conversation = conversationRepo.findConversationBetween(fromId, toId)
                .orElseGet(() -> createConversationWithParticipants(fromId, toId));
        conversation.setLastMessageAt(now);
        conversation.setLastMessagePreview(content);
        ChatParticipant participant = participantRepo.findByIdOrThrow(new ChatParticipantId(conversation.getId(), fromId));
        participant.setLastReadAt(now);
        User from = userRepo.findByIdOrThrow(fromId);
        ChatMessage message = messageRepo.save(ChatMessage.builder()
                .conversation(conversation).sender(from).sentAt(now).content(content).build());
        ChatMessageDto messageDto = chatMapper.map(message);
        log.info("Successfully saved ChatMessage");
        return messageDto;
    }

    private ChatConversation createConversationWithParticipants(UUID fromId, UUID toId) {
        log.debug("Trying to create ChatConversation from User {} to User {}", fromId, toId);
        User to = userRepo.findByIdOrThrow(toId);
        User from = userRepo.findByIdOrThrow(fromId);
        ChatConversation conversation = conversationRepo.save(new ChatConversation());
        ChatParticipant participantFrom = ChatParticipant.builder()
                .id(new ChatParticipantId()).user(from).conversation(conversation).build();
        ChatParticipant participantTo = ChatParticipant.builder()
                .id(new ChatParticipantId()).user(to).conversation(conversation).build();
        participantRepo.saveAll(List.of(participantFrom, participantTo));
        log.info("Successfully created ChatConversation");
        return conversation;
    }

    public ChatConversationDto startChatConversation(UUID myId, UUID otherId) {
        log.debug("Trying to start a ChatConversation between User {} and User {}", myId, otherId);
        ChatConversation conversation = conversationRepo.findConversationBetween(myId, otherId)
                .orElseGet(() -> createConversationWithParticipants(myId, otherId));
        UserProfile otherProfile = userRepo.findByIdOrThrow(otherId).getProfile();
        ChatConversationDto conversationDto = chatMapper.map(conversation, otherProfile);
        log.info("Successfully started ChatConversation");
        return conversationDto;
    }

    public void readMessage(UUID userId, UUID conversationId) {
        log.debug("Trying to update lastReadAt in ChatConversation {} for User {}", conversationId, userId);
        ChatParticipant participant = participantRepo.findByIdOrThrow(new ChatParticipantId(conversationId, userId));
        participant.setLastReadAt(LocalDateTime.now());
        participantRepo.save(participant);
        log.info("Successfully updated lastReadAt in ChatConversation");
    }

}
