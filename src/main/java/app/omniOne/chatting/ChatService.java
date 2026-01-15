package app.omniOne.chatting;

import app.omniOne.chatting.model.ChatMapper;
import app.omniOne.chatting.model.dto.ChatDto;
import app.omniOne.chatting.model.dto.ChatMessageDto;
import app.omniOne.chatting.model.dto.ChatsDto;
import app.omniOne.chatting.model.entity.ChatConversation;
import app.omniOne.chatting.model.entity.ChatMessage;
import app.omniOne.chatting.model.entity.ChatParticipant;
import app.omniOne.chatting.model.entity.ChatParticipantId;
import app.omniOne.chatting.repository.ChatConversationRepo;
import app.omniOne.chatting.repository.ChatMessageRepo;
import app.omniOne.chatting.repository.ChatParticipantRepo;
import app.omniOne.model.entity.User;
import app.omniOne.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepo userRepo;
    private final ChatMapper chatMapper;
    private final ChatMessageRepo messageRepo;
    private final ChatParticipantRepo participantRepo;
    private final ChatConversationRepo conversationRepo;

    public List<ChatsDto> getChats(UUID userId) {
        return conversationRepo.findChats(userId);
    }

    public ChatDto getChat(UUID conversationId) {
        ChatConversation conversation = conversationRepo.findByIdOrThrow(conversationId);
        List<ChatMessageDto> messages = messageRepo.findAllByConversationIdOrderBySentAtDesc(conversationId)
                .stream().map(chatMapper::map).toList();
        return new ChatDto(conversationId, conversation.getCreatedAt(), messages);
    }

    @Transactional
    public void saveMessage(UUID fromId, UUID toId, String content) {
        LocalDateTime now = LocalDateTime.now();
        User from = userRepo.findByIdOrThrow(fromId);
        ChatConversation conversation = conversationRepo.findConversationBetween(fromId, toId)
                .orElseGet(() -> createConversationWithParticipants(fromId, toId, from, now));
        conversation.setLastMessageAt(now);
        ChatMessage message = ChatMessage.builder().conversation(conversation).sender(from).content(content).build();
        messageRepo.save(message);
    }

    private ChatConversation createConversationWithParticipants(
            UUID fromId, UUID toId, User from, LocalDateTime now) {
        User to = userRepo.findByIdOrThrow(toId);
        ChatConversation conversation = conversationRepo.save(ChatConversation.builder()
                .lastMessageAt(now).build());
        ChatParticipant participantFrom = ChatParticipant.builder()
                .id(new ChatParticipantId()).user(from).conversation(conversation).lastReadAt(now).build();
        ChatParticipant participantTo = ChatParticipant.builder()
                .id(new ChatParticipantId()).user(to).conversation(conversation).build();
        participantRepo.saveAll(List.of(participantFrom, participantTo));
        return conversation;
    }

}
