package app.omniOne.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemDetailAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProblemDetailAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        ProblemDetailFactory factory = new ProblemDetailFactory(objectMapper);
        entryPoint = new ProblemDetailAuthenticationEntryPoint(factory);
    }

    @Test
    void disabledException_returnsForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new DisabledException("disabled"));

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(403, response.getStatus());
        assertEquals("AUTH_ACCOUNT_DISABLED", extractErrorCode(body));
    }

    @Test
    void badCredentials_returnsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertEquals("AUTH_INVALID_CREDENTIALS", extractErrorCode(body));
    }

    private String extractErrorCode(JsonNode body) {
        JsonNode topLevel = body.path("errorCode");
        if (!topLevel.isMissingNode() && !topLevel.isNull()) {
            return topLevel.asText();
        }
        JsonNode nested = body.path("properties").path("errorCode");
        if (!nested.isMissingNode() && !nested.isNull()) {
            return nested.asText();
        }
        return null;
    }
}
