package app.omniOne.chatting;

import app.omniOne.chatting.model.dto.ChatMessageDto;
import app.omniOne.chatting.model.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    @PreAuthorize("@authService.isRelated(#principal.name, #request.to())")
    public void sendMessage(ChatMessageRequest request, Principal principal) {
        ChatMessageDto message = chatService.saveMessage(
                UUID.fromString(principal.getName()), request.to(), request.content());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.to()), "/queue/reply", message);
    }

    @MessageMapping("/chat.read")
    @PreAuthorize("@authService.isChatOf(#principal.name, #conversationId)")
    public void readMessage(UUID conversationId, Principal principal) {
        chatService.readMessage(UUID.fromString(principal.getName()), conversationId);
    }

}
