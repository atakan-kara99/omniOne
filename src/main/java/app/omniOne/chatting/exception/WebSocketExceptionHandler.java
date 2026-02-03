package app.omniOne.chatting.exception;

import app.omniOne.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    @SendToUser("/queue/errors")
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public WebSocketError handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        BindingResult result = ex.getBindingResult();
        if (result != null)
            result.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        log.warn("Failed to validate websocket message");
        return new WebSocketError(
                "Validation Error",
                "Message could not be sent",
                ErrorCode.VALIDATION_ERROR.name(),
                MDC.get("traceId"),
                errors);
    }

    @SendToUser("/queue/errors")
    @MessageExceptionHandler(Exception.class)
    public WebSocketError handleUnexpected(Exception ex) {
        log.error("Server Error", ex);
        return new WebSocketError(
                "Server Error",
                "Message failed",
                ErrorCode.INTERNAL_ERROR.name(),
                MDC.get("traceId"),
                Map.of());
    }

}
