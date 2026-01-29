package app.omniOne.chatting.exception;

import java.util.Map;

public record WebSocketError(

        String type,

        String message,

        Map<String, String> fieldErrors

) {
}
