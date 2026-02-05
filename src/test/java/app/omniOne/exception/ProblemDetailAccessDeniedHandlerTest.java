package app.omniOne.exception;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemDetailAccessDeniedHandlerTest {

    @Test
    void maskToken_exposesOnlyFirstAndLastTwoCharacters() throws Exception {
        ProblemDetailAccessDeniedHandler handler = new ProblemDetailAccessDeniedHandler(null);

        assertEquals("ab...yz(len=8)", invokeMaskToken(handler, "ab12wxyz"));
        assertEquals("****(len=3)", invokeMaskToken(handler, "abc"));
        assertEquals("null", invokeMaskToken(handler, null));
    }

    private String invokeMaskToken(ProblemDetailAccessDeniedHandler handler, String token) throws Exception {
        Method method = ProblemDetailAccessDeniedHandler.class.getDeclaredMethod("maskToken", String.class);
        method.setAccessible(true);
        return (String) method.invoke(handler, token);
    }
}
