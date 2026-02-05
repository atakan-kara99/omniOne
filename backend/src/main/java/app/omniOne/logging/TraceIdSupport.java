package app.omniOne.logging;

import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class TraceIdSupport {

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]{1,64}$");
    private static final int TRACE_BASE_LENGTH = 8;

    public static String resolveHttpTraceId(String inboundRequestId) {
        if (inboundRequestId != null) {
            String trimmed = inboundRequestId.trim();
            if (REQUEST_ID_PATTERN.matcher(trimmed).matches()) {
                return trimmed;
            }
        }
        return randomBase() + "HT";
    }

    public static String resolveWebSocketTraceId(String sessionId) {
        if (sessionId != null) {
            String trimmed = sessionId.trim();
            if (!trimmed.isBlank()) {
                String base = trimmed.length() <= TRACE_BASE_LENGTH
                        ? trimmed
                        : trimmed.substring(0, TRACE_BASE_LENGTH);
                return base + "WS";
            }
        }
        return randomBase() + "WS";
    }

    private static String randomBase() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, TRACE_BASE_LENGTH);
    }
}
