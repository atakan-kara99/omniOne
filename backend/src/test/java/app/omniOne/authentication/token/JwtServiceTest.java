package app.omniOne.authentication.token;

import app.omniOne.authentication.model.UserDetails;
import app.omniOne.exception.custom.JwtInvalidException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static app.omniOne.TestFixtures.clientEmail;
import static app.omniOne.TestFixtures.userEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) class JwtServiceTest {

    @Mock private UserDetails userDetails;

    private JwtService jwtService;

    @BeforeEach void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "applicationName", "omniOne");
        ReflectionTestUtils.setField(jwtService, "authSecret", "auth-secret");
        ReflectionTestUtils.setField(jwtService, "initSecret", "init-secret");
        ReflectionTestUtils.setField(jwtService, "authorizationTtlMins", 15);
        ReflectionTestUtils.setField(jwtService, "resetPasswordTtlMins", 15);
        ReflectionTestUtils.setField(jwtService, "activationTtlMins", 60 * 24);
        ReflectionTestUtils.setField(jwtService, "invitationTtlMins", 60 * 24);
        jwtService.init();
    }

    @Test void createAuthJwt_containsExpectedClaimsAndIsVerifiable() {
        UUID userId = UUID.randomUUID();
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getRole()).thenReturn("COACH");

        String token = jwtService.createAuthJwt(userDetails);
        DecodedJWT decoded = jwtService.verifyAuth(token);

        assertEquals("authorization", decoded.getSubject());
        assertEquals("omniOne", decoded.getIssuer());
        assertEquals(userId.toString(), decoded.getClaim("id").asString());
        assertEquals("COACH", decoded.getClaim("role").asString());
        assertTrue(isExpiresWithin(decoded, Duration.ofMinutes(15)));
    }

    @Test void createResetPasswordJwt_isVerifiableWithEmailClaim() {
        String token = jwtService.createResetPasswordJwt(userEmail);

        DecodedJWT decoded = jwtService.verifyResetPassword(token);

        assertEquals("reset-password", decoded.getSubject());
        assertEquals(userEmail, decoded.getClaim("email").asString());
        assertTrue(isExpiresWithin(decoded, Duration.ofMinutes(15)));
    }

    @Test void createActivationJwt_usesInitSecretAndContainsEmail() {
        String token = jwtService.createActivationJwt(userEmail);

        DecodedJWT decoded = jwtService.verifyActivation(token);

        assertEquals("activation", decoded.getSubject());
        assertEquals(userEmail, decoded.getClaim("email").asString());
        assertTrue(isExpiresWithin(decoded, Duration.ofMinutes(60 * 24)));
    }

    @Test void createInvitationJwt_containsClientEmailAndCoachId() {
        UUID coachId = UUID.randomUUID();

        String token = jwtService.createInvitationJwt(clientEmail, coachId);

        DecodedJWT decoded = jwtService.verifyInvitation(token);

        assertEquals("invitation", decoded.getSubject());
        assertEquals(clientEmail, decoded.getClaim("clientEmail").asString());
        assertEquals(coachId.toString(), decoded.getClaim("coachId").asString());
        assertTrue(isExpiresWithin(decoded, Duration.ofMinutes(60 * 24)));
    }

    @Test void verifyActivation_rejectsInvitationToken() {
        String invitationToken = jwtService.createInvitationJwt(clientEmail, UUID.randomUUID());

        assertThrows(JwtInvalidException.class, () -> jwtService.verifyActivation(invitationToken));
    }

    @Test void verifyResetPassword_rejectsAuthorizationToken() {
        UUID userId = UUID.randomUUID();
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getRole()).thenReturn("ROLE_CLIENT");
        String authorizationToken = jwtService.createAuthJwt(userDetails);

        assertThrows(JwtInvalidException.class, () -> jwtService.verifyResetPassword(authorizationToken));
    }

    private boolean isExpiresWithin(DecodedJWT jwt, Duration expectedDuration) {
        long secondsUntilExpiry = Duration.between(Instant.now(), jwt.getExpiresAt().toInstant()).toSeconds();
        return secondsUntilExpiry > 0 && secondsUntilExpiry <= expectedDuration.plusSeconds(10).toSeconds();
    }
}
