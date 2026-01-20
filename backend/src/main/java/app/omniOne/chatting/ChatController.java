package app.omniOne.chatting;

import app.omniOne.chatting.model.dto.ChatRequest;
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

    @MessageMapping("/chat")
    @PreAuthorize("@authService.isRelated(#principal.name, #request.to())")
    public void sendPrivateMessage(ChatMessageRequest request, Principal principal) {
        ChatMessageDto message = chatService.saveMessage(
                UUID.fromString(principal.getName()), request.to(), request.content());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.to()), "/queue/reply", request.content());
    }

}
