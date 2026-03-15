package app.omniOne.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProblemDetailFactory {

    private final ObjectMapper objectMapper;

    public ProblemDetail create(HttpServletRequest request,
                                HttpStatus status,
                                ErrorCode errorCode,
                                String title,
                                String detail,
                                Map<String, Object> extraProperties) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        if (detail != null && !detail.isBlank()) {
            pd.setDetail(detail);
        }
        pd.setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        pd.setProperty("errorCode", errorCode.name());
        if (request != null) {
            pd.setProperty("path", request.getRequestURI());
        }
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }
        if (extraProperties != null && !extraProperties.isEmpty()) {
            extraProperties.forEach(pd::setProperty);
        }
        return pd;
    }

    public void write(HttpServletRequest request,
                      HttpServletResponse response,
                      HttpStatus status,
                      ErrorCode errorCode,
                      String title,
                      String detail,
                      Map<String, Object> extraProperties) throws IOException {
        ProblemDetail pd = create(request, status, errorCode, title, detail, extraProperties);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }

}
