package app.omniOne.chatting;

import app.omniOne.authentication.token.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthChannelInterceptorTest {

    @Mock private JwtService jwtService;
    @Mock private ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;
    @Mock private MessageChannel channel;

    @Test
    void rejectsMissingAuthorizationHeaderWithShortSessionId() {
        when(messagingTemplateProvider.getIfAvailable()).thenReturn(null);
        AuthChannelInterceptor interceptor = new AuthChannelInterceptor(jwtService, messagingTemplateProvider);
        Message<?> message = connectMessageWithSessionId("a");

        Message<?> result = interceptor.preSend(message, channel);

        assertNull(result);
    }

    private Message<?> connectMessageWithSessionId(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId(sessionId);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
