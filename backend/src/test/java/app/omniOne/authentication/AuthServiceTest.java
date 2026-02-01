package app.omniOne.authentication;

import app.omniOne.authentication.model.UserDetails;
import app.omniOne.authentication.model.dto.LoginRequest;
import app.omniOne.authentication.model.dto.LoginResponse;
import app.omniOne.authentication.model.dto.PasswordRequest;
import app.omniOne.authentication.model.dto.RegisterRequest;
import app.omniOne.authentication.token.JwtService;
import app.omniOne.authentication.token.RefreshTokenService;
import app.omniOne.chatting.repository.ChatParticipantRepo;
import app.omniOne.email.EmailService;
import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.User;
import app.omniOne.model.enums.UserRole;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.CoachingRepo;
import app.omniOne.repository.UserRepo;
import app.omniOne.service.CoachingService;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static app.omniOne.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class AuthServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private CoachRepo coachRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder encoder;
    @Mock private EmailService emailService;
    @Mock private CoachingRepo coachingRepo;
    @Mock private CoachingService coachingService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ChatParticipantRepo chatParticipantRepo;
    @Mock private RefreshTokenService refreshTokenService;
    @InjectMocks private AuthService authService;

    @AfterEach void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test void isCoachedByMe_returnsTrueWhenCoachMatches() {
        UUID coachId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Client client = new Client();
        Coach coach = new Coach();
        coach.setId(coachId);
        client.setCoach(coach);
        when(coachingRepo.existsByCoachIdAndClientId(coachId, clientId)).thenReturn(true);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(coachId.toString(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean result = authService.isCoachedByMe(clientId);

        assertTrue(result);
        verify(coachingRepo).existsByCoachIdAndClientId(coachId, clientId);
    }

    @Test void login_returnsJwtAfterAuthentication() {
        LoginRequest request = new LoginRequest(userEmail, "pass");
        UUID deviceId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = mock(UserDetails.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.createAuthJwt(principal)).thenReturn("jwt-token");
        when(refreshTokenService.generateToken()).thenReturn("refresh-token");
        when(principal.getUser()).thenReturn(user);

        LoginResponse response = authService.login(request, deviceId);

        assertEquals("jwt-token", response.jwt());
        assertEquals("refresh-token", response.refreshToken());
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtService).createAuthJwt(principal);
        verify(refreshTokenService).generateToken();
        verify(refreshTokenService).saveRefreshToken("refresh-token", user, deviceId);
    }

    @Test void register_savesCoachAndSendsActivationMail() {
        RegisterRequest dto = new RegisterRequest(" Coach@Omni.One ", "pwd");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = User.builder().id(UUID.randomUUID()).enabled(false).email(coachEmail).build();
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(userRepo.findByEmail(coachEmail)).thenReturn(java.util.Optional.empty());
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(userRepo.findByEmailOrThrow(coachEmail)).thenReturn(savedUser);
        when(jwtService.createActivationJwt(coachEmail)).thenReturn("activation-jwt");

        User result = authService.registerCoach(dto);

        assertSame(savedUser, result);
        verify(userRepo).findByEmail(coachEmail);
        verify(userRepo).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertEquals(coachEmail, captured.getEmail());
        assertEquals("encoded", captured.getPassword());
        assertEquals(UserRole.COACH, captured.getRole());
        verify(coachRepo).save(any(Coach.class));
        verify(emailService).sendActivationMail(coachEmail, "activation-jwt");
    }

    @Test void register_throwsForAdminRole() {
        RegisterRequest dto = new RegisterRequest(adminEmail, "pwd");

        User existing = new User();
        existing.setEnabled(false);
        existing.setRole(UserRole.ADMIN);
        when(userRepo.findByEmail(adminEmail)).thenReturn(java.util.Optional.of(existing));
        assertThrows(NotAllowedException.class, () -> authService.registerCoach(dto));
    }

    @Test void register_throwsForDuplicateEmail() {
        RegisterRequest dto = new RegisterRequest(userEmail, "pwd");
        User existing = new User();
        existing.setEnabled(true);
        when(userRepo.findByEmail(userEmail)).thenReturn(java.util.Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> authService.registerCoach(dto));
    }

    @Test void activate_enablesUserFromVerifiedToken() {
        User user = new User();
        user.setEnabled(false);
        user.setEmail(userEmail);
        User savedUser = new User();
        savedUser.setEnabled(true);
        DecodedJWT jwt = mockJwt("email", userEmail);
        when(jwtService.verifyActivation("token")).thenReturn(jwt);
        when(userRepo.findByEmailOrThrow(userEmail)).thenReturn(user);
        when(userRepo.save(user)).thenReturn(savedUser);

        User result = authService.activate("token");

        assertSame(savedUser, result);
        assertTrue(user.isEnabled());
        verify(userRepo).save(user);
    }

    @Test void sendActivationMail_throwsWhenAlreadyEnabled() {
        User user = new User();
        user.setEnabled(true);
        when(userRepo.findByEmailOrThrow(userEmail)).thenReturn(user);

        assertThrows(NotAllowedException.class, () -> authService.sendActivationMail(userEmail));
    }

    @Test void sendActivationMail_sendsMailWhenNotEnabled() {
        User user = new User();
        user.setEnabled(false);
        when(userRepo.findByEmailOrThrow(userEmail)).thenReturn(user);
        when(jwtService.createActivationJwt(userEmail)).thenReturn("jwt");

        authService.sendActivationMail(userEmail);

        verify(emailService).sendActivationMail(userEmail, "jwt");
    }

    @Test void sendInvitationMail_createsAndSendsToken() {
        UUID coachId = UUID.randomUUID();
        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(new Coach());
        when(jwtService.createInvitationJwt(clientEmail, coachId)).thenReturn("jwt");

        authService.sendInvitationMail(clientEmail, coachId);

        verify(emailService).sendInvitationMail(clientEmail, "jwt");
    }

    @Test void acceptInvitation_registersClientAndStartsCoaching() {
        UUID coachId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim emailClaim = mock(Claim.class);
        Claim coachClaim = mock(Claim.class);
        when(jwt.getClaim("clientEmail")).thenReturn(emailClaim);
        when(jwt.getClaim("coachId")).thenReturn(coachClaim);
        when(emailClaim.asString()).thenReturn(clientEmail);
        when(coachClaim.asString()).thenReturn(coachId.toString());
        when(jwtService.verifyInvitation("token")).thenReturn(jwt);
        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(new Coach());
        when(userRepo.findByEmail(clientEmail)).thenReturn(java.util.Optional.empty());
        when(encoder.encode("pwd")).thenReturn("encoded");
        User savedUser = new User();
        savedUser.setId(clientId);
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        Client savedClient = new Client();
        savedClient.setId(clientId);
        when(clientRepo.save(any(Client.class))).thenReturn(savedClient);

        User result = authService.acceptInvitation("token", new PasswordRequest("pwd"));

        assertSame(savedUser, result);
        verify(coachingService).startCoaching(coachId, clientId);
    }

    @Test void sendForgotMail_createsResetTokenAndSendsMail() {
        User user = new User();
        user.setEnabled(true);
        when(userRepo.findByEmailOrThrow(userEmail)).thenReturn(user);
        when(jwtService.createResetPasswordJwt(userEmail)).thenReturn("reset-jwt");

        authService.sendForgotMail(userEmail);

        verify(emailService).sendResetPasswordMail(userEmail, "reset-jwt");
    }

    @Test void reset_setsEncodedPasswordAndSavesUser() {
        User user = new User();
        user.setEmail(userEmail);
        User saved = new User();
        DecodedJWT jwt = mockJwt("email", userEmail);
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(jwtService.verifyResetPassword("token")).thenReturn(jwt);
        when(userRepo.findByEmailOrThrow(userEmail)).thenReturn(user);
        when(userRepo.save(user)).thenReturn(saved);

        User result = authService.reset("token", new PasswordRequest("new-pwd"));

        assertSame(saved, result);
        assertEquals("encoded", user.getPassword());
        verify(encoder).encode("new-pwd");
    }

    private DecodedJWT mockJwt(String claimName, String value) {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(jwt.getClaim(claimName)).thenReturn(claim);
        when(claim.asString()).thenReturn(value);
        return jwt;
    }
}
