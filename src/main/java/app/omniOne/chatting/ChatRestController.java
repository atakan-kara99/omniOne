package app.omniOne.chatting;

import app.omniOne.chatting.model.ChatMapper;
import app.omniOne.chatting.model.dto.ChatConversationDto;
import app.omniOne.chatting.model.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/chats")
public class ChatRestController {

    private final ChatMapper chatMapper;
    private final ChatService chatService;

    @GetMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authService.isChatOf(@authService.getMyId(), #conversationId)")
    public Slice<ChatMessageDto> getSliceOfMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) LocalDateTime beforeSentAt,
            @RequestParam(defaultValue = "10") int size) {
        if (beforeSentAt == null)
            chatService.readMessage(getMyId(), conversationId);
        return chatService.getSliceOfMessages(conversationId, beforeSentAt, size).map(chatMapper::map);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ChatConversationDto> getChatConversations() {
        return chatService.getChatConversations(getMyId());
    }

    @GetMapping("/start/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authService.isRelated(@authService.getMyId(), #userId)")
    public ChatConversationDto startChatConversation(@PathVariable UUID userId) {
        return chatService.startChatConversation(getMyId(), userId);
    }

}




