package app.omniOne.chatting.exception;

import java.util.Map;

public record WebSocketError(

        String type,

        String message,

        String errorCode,

        String traceId,

        Map<String, String> fieldErrors

) {}
