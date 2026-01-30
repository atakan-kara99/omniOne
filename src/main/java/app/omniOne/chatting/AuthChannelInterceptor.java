package app.omniOne.chatting;

import app.omniOne.authentication.token.JwtService;
import app.omniOne.chatting.exception.WebSocketError;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null)
            return message;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Failed to read authorization header on STOMP CONNECT");
                sendError(accessor, "Authentication Error", "Missing or invalid Authorization header");
                return null;
            }
            String jwt = authHeader.substring(7);
            try {
                DecodedJWT decodedJwt = jwtService.verifyAuth(jwt);
                String id = decodedJwt.getClaim("id").asString();
                String role = decodedJwt.getClaim("role").asString();
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                id, null, Collections.singleton(new SimpleGrantedAuthority(role)));
                accessor.setUser(authToken);
            } catch (Exception ex) {
                log.warn("Failed to authenticate STOMP CONNECT: {}", ex.getMessage());
                sendError(accessor, "Authentication Error", "Invalid token");
                return null;
            }
        }
        return message;
    }

    private void sendError(StompHeaderAccessor accessor, String type, String message) {
        if (accessor == null || accessor.getSessionId() == null)
            return;
        WebSocketError error = new WebSocketError(type, message, Map.of());
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setSessionId(accessor.getSessionId());
        headers.setLeaveMutable(true);
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate == null)
            return;
        messagingTemplate.convertAndSendToUser(
                accessor.getSessionId(), "/queue/errors", error, headers.getMessageHeaders());
    }

}
