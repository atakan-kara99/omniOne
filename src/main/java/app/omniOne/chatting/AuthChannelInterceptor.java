package app.omniOne.chatting;

import app.omniOne.authentication.jwt.JwtService;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null)
            return message;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Failed to read authorization header on STOMP CONNECT");
                return null; // or throw
            }
            String jwt = authHeader.substring(7);
            DecodedJWT decodedJwt = jwtService.verifyAuth(jwt);
            String id = decodedJwt.getClaim("id").asString();
            String role = decodedJwt.getClaim("role").asString();
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            id, null, Collections.singleton(new SimpleGrantedAuthority(role)));
            accessor.setUser(authToken);
        }
        return message;
    }

}
