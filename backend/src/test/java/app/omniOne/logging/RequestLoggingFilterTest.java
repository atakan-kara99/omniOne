package app.omniOne.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void preservesValidInboundRequestIdAndClearsMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "my-request-id_1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> assertEquals("my-request-id_1", MDC.get("traceId"));

        filter.doFilterInternal(request, response, chain);

        assertEquals("my-request-id_1", response.getHeader("X-Request-Id"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void replacesUnsafeInboundRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "bad\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> assertTrue(MDC.get("traceId").matches("^[a-f0-9]{8}HT$"));

        filter.doFilterInternal(request, response, chain);

        assertTrue(response.getHeader("X-Request-Id").matches("^[a-f0-9]{8}HT$"));
        assertNull(MDC.get("traceId"));
    }
}
