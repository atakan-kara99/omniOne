package app.omniOne.chatting;

import app.omniOne.chatting.model.dto.ChatMessageAckDto;
import app.omniOne.chatting.model.dto.ChatMessageDto;
import app.omniOne.chatting.model.dto.ChatMessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;
import java.util.UUID;

@Validated
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    @PreAuthorize("@authService.isRelated(#principal.name, #request.to())")
    public void sendMessage(@Payload @Valid ChatMessageRequest request, Principal principal) {
        UUID fromId = UUID.fromString(principal.getName());
        ChatMessageDto message = chatService.saveMessage(
                fromId, request.to(), request.content());
        ChatMessageAckDto senderAck = new ChatMessageAckDto(
                request.clientMessageId(), message.messageId(),
                message.conversationId(), message.sentAt());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.to()), "/queue/reply", message);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(fromId), "/queue/acks", senderAck);
    }

    @MessageMapping("/chat.read")
    @PreAuthorize("@authService.isChatOf(#principal.name, #conversationId)")
    public void readMessage(@Payload UUID conversationId, Principal principal) {
        chatService.readMessage(UUID.fromString(principal.getName()), conversationId);
    }

}
