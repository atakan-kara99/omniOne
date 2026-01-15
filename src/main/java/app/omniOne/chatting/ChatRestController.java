package app.omniOne.chatting;

import app.omniOne.chatting.model.dto.ChatDto;
import app.omniOne.chatting.model.dto.ChatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/chats")
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping
    public List<ChatsDto> getChats() {
        return chatService.getChats(getMyId());
    }

    @GetMapping("/{conversationId}")
    @PreAuthorize("@authService.isMyChat(#conversationId)")
    public ChatDto getChat(@PathVariable UUID conversationId) {
        return chatService.getChat(conversationId);
    }

}




