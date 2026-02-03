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
import app.omniOne.repository.UserProfileRepo;
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
    private final UserProfileRepo userProfileRepo;
    private final ChatParticipantRepo participantRepo;
    private final ChatConversationRepo conversationRepo;

    public List<ChatConversationDto> getChatConversations(UUID userId) {
        return conversationRepo.findConversationsOf(userId);
    }

    public Slice<ChatMessage> getSliceOfMessages(UUID conversationId, LocalDateTime beforeSentAt, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<ChatMessage> chatMessages;
        if (beforeSentAt == null) {
            chatMessages = messageRepo.findByConversationIdOrderBySentAtDescIdDesc(conversationId, pageable);
        } else {
            chatMessages = messageRepo.findMessagesOlderThan(conversationId, beforeSentAt, pageable);
        }
        return chatMessages;
    }

    @Transactional
    public ChatMessageDto saveMessage(UUID fromId, UUID toId, String content) {
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
        return chatMapper.map(message);
    }

    private ChatConversation createConversationWithParticipants(UUID fromId, UUID toId) {
        User to = userRepo.findByIdOrThrow(toId);
        User from = userRepo.findByIdOrThrow(fromId);
        ChatConversation conversation = conversationRepo.save(new ChatConversation());
        ChatParticipant participantFrom = ChatParticipant.builder()
                .id(new ChatParticipantId()).user(from).conversation(conversation).build();
        ChatParticipant participantTo = ChatParticipant.builder()
                .id(new ChatParticipantId()).user(to).conversation(conversation).build();
        participantRepo.saveAll(List.of(participantFrom, participantTo));
        return conversation;
    }

    public ChatConversationDto startChatConversation(UUID myId, UUID otherId) {
        ChatConversation conversation = conversationRepo.findConversationBetween(myId, otherId)
                .orElseGet(() -> createConversationWithParticipants(myId, otherId));
        UserProfile otherProfile = userProfileRepo.findByIdOrThrow(otherId);
        return chatMapper.map(conversation, otherProfile);
    }

    public void readMessage(UUID userId, UUID conversationId) {
        ChatParticipant participant = participantRepo.findByIdOrThrow(new ChatParticipantId(conversationId, userId));
        participant.setLastReadAt(LocalDateTime.now());
        participantRepo.save(participant);
    }

}
