package app.omniOne.authentication.token;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class JwtFilterTest {

    @Mock private JwtService jwtService;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtFilter jwtFilter;

    @AfterEach void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test void doesNothingWhenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());
    }

    @Test void setsAuthenticationWhenTokenValid() throws Exception {
        String token = "valid-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/coach/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        Claim idClaim = mock(Claim.class);
        Claim roleClaim = mock(Claim.class);
        when(decodedJWT.getClaim("id")).thenReturn(idClaim);
        when(decodedJWT.getClaim("role")).thenReturn(roleClaim);
        when(idClaim.asString()).thenReturn("user-123");
        when(roleClaim.asString()).thenReturn("ROLE_COACH");
        when(jwtService.verifyAuth(token)).thenReturn(decodedJWT);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).verifyAuth(token);
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user-123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_COACH")));
    }

    @Test void returnsUnauthorizedWhenTokenInvalid() throws Exception {
        String token = "invalid-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/coach/clients");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.verifyAuth(token)).thenThrow(new RuntimeException("boom"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).verifyAuth(token);
        verifyNoInteractions(filterChain);
        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Map<String, Object> body = new ObjectMapper()
                .readValue(response.getContentAsByteArray(), new TypeReference<>() {});
        assertEquals("Invalid JWToken", body.get("error"));
        assertEquals("/coach/clients", body.get("path"));
        assertEquals(401, body.get("status"));
    }

}
