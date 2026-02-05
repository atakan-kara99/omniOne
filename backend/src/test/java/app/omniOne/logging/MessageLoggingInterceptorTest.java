package app.omniOne.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageLoggingInterceptorTest {

    private final MessageLoggingInterceptor interceptor = new MessageLoggingInterceptor();

    @Test
    void usesShortSessionIdWithoutCrashing() {
        Message<?> message = messageWithSessionId("abc");

        interceptor.preSend(message, null);

        assertEquals("abcWS", MDC.get("traceId"));
        interceptor.afterSendCompletion(message, null, true, null);
        assertNull(MDC.get("traceId"));
    }

    @Test
    void truncatesLongSessionIdToEightChars() {
        Message<?> message = messageWithSessionId("1234567890");

        interceptor.beforeHandle(message, null, null);

        assertEquals("12345678WS", MDC.get("traceId"));
        interceptor.afterMessageHandled(message, null, null, null);
        assertNull(MDC.get("traceId"));
    }

    private Message<?> messageWithSessionId(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionId(sessionId);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
