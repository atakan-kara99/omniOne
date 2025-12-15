package app.omniOne.authentication;

import app.omniOne.authentication.jwt.JwtService;
import app.omniOne.authentication.model.LoginRequest;
import app.omniOne.authentication.model.PasswordRequest;
import app.omniOne.authentication.model.RegisterRequest;
import app.omniOne.authentication.model.UserDetails;
import app.omniOne.email.EmailService;
import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.User;
import app.omniOne.model.enums.UserRole;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private CoachRepo coachRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder encoder;
    @Mock private EmailService emailService;
    @Mock private CoachingService coachingService;
    @Mock private AuthenticationManager authenticationManager;
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
        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(coachId.toString(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean result = authService.isCoachedByMe(clientId);

        assertTrue(result);
        verify(clientRepo).findByIdOrThrow(clientId);
    }

    @Test void login_returnsJwtAfterAuthentication() {
        LoginRequest request = new LoginRequest("user@mail.com", "pass");
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = mock(UserDetails.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.createAuthJwt(principal)).thenReturn("jwt-token");

        String token = authService.login(request);

        assertEquals("jwt-token", token);
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtService).createAuthJwt(principal);
    }

    @Test void register_savesCoachAndSendsActivationMail() {
        RegisterRequest dto = new RegisterRequest(" Coach@Email.com ", "pwd", UserRole.COACH);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = User.builder().id(UUID.randomUUID()).build();
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(userRepo.existsByEmail("coach@email.com")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.createActivationJwt("coach@email.com")).thenReturn("activation-jwt");

        User result = authService.register(dto);

        assertSame(savedUser, result);
        verify(userRepo).existsByEmail("coach@email.com");
        verify(userRepo).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertEquals("coach@email.com", captured.getEmail());
        assertEquals("encoded", captured.getPassword());
        assertEquals(UserRole.COACH, captured.getRole());
        verify(coachRepo).save(any(Coach.class));
        verify(emailService).sendActivationMail("coach@email.com", "activation-jwt");
    }

    @Test void register_throwsForAdminRole() {
        RegisterRequest dto = new RegisterRequest("admin@mail.com", "pwd", UserRole.ADMIN);

        assertThrows(NotAllowedException.class, () -> authService.register(dto));
    }

    @Test void register_throwsForDuplicateEmail() {
        RegisterRequest dto = new RegisterRequest("user@mail.com", "pwd", UserRole.CLIENT);
        when(userRepo.existsByEmail("user@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(dto));
    }

    @Test void activate_enablesUserFromVerifiedToken() {
        String email = "user@mail.com";
        User user = new User();
        user.setEnabled(false);
        user.setEmail(email);
        User savedUser = new User();
        savedUser.setEnabled(true);
        DecodedJWT jwt = mockJwt("email", email);
        when(jwtService.verifyActivation("token")).thenReturn(jwt);
        when(userRepo.findByEmailOrThrow(email)).thenReturn(user);
        when(userRepo.save(user)).thenReturn(savedUser);

        User result = authService.activate("token");

        assertSame(savedUser, result);
        assertTrue(user.isEnabled());
        verify(userRepo).save(user);
    }

    @Test void sendActivationMail_throwsWhenAlreadyEnabled() {
        User user = new User();
        user.setEnabled(true);
        when(userRepo.findByEmailOrThrow("user@mail.com")).thenReturn(user);

        assertThrows(NotAllowedException.class, () -> authService.sendActivationMail("user@mail.com"));
    }

    @Test void sendActivationMail_sendsMailWhenNotEnabled() {
        User user = new User();
        user.setEnabled(false);
        when(userRepo.findByEmailOrThrow("user@mail.com")).thenReturn(user);
        when(jwtService.createActivationJwt("user@mail.com")).thenReturn("jwt");

        authService.sendActivationMail("user@mail.com");

        verify(emailService).sendActivationMail("user@mail.com", "jwt");
    }

    @Test void sendInvitationMail_createsAndSendsToken() {
        UUID coachId = UUID.randomUUID();
        when(userRepo.findByIdOrThrow(coachId)).thenReturn(new User());
        when(jwtService.createInvitationJwt("client@mail.com", coachId)).thenReturn("jwt");

        authService.sendInvitationMail("client@mail.com", coachId);

        verify(emailService).sendInvitationMail("client@mail.com", "jwt");
    }

    @Test void acceptInvitation_registersClientAndStartsCoaching() {
        UUID coachId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim emailClaim = mock(Claim.class);
        Claim coachClaim = mock(Claim.class);
        when(jwt.getClaim("clientEmail")).thenReturn(emailClaim);
        when(jwt.getClaim("coachId")).thenReturn(coachClaim);
        when(emailClaim.asString()).thenReturn("client@mail.com");
        when(coachClaim.asString()).thenReturn(coachId.toString());
        when(jwtService.verifyInvitation("token")).thenReturn(jwt);
        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(new Coach());
        User registered = new User();
        registered.setId(clientId);
        AuthService spyService = spy(authService);
        doReturn(registered).when(spyService).register(any(RegisterRequest.class));
        Client client = new Client();
        client.setId(clientId);
        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);

        User result = spyService.acceptInvitation("token", new PasswordRequest("pwd"));

        assertSame(registered, result);
        verify(coachingService).startCoaching(coachId, clientId);
    }

    @Test void sendForgotMail_createsResetTokenAndSendsMail() {
        when(userRepo.findByEmailOrThrow("user@mail.com")).thenReturn(new User());
        when(jwtService.createResetPasswordJwt("user@mail.com")).thenReturn("reset-jwt");

        authService.sendForgotMail("user@mail.com");

        verify(emailService).sendResetPasswordMail("user@mail.com", "reset-jwt");
    }

    @Test void reset_setsEncodedPasswordAndSavesUser() {
        String email = "user@mail.com";
        User user = new User();
        user.setEmail(email);
        User saved = new User();
        DecodedJWT jwt = mockJwt("email", email);
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(jwtService.verifyResetPassword("token")).thenReturn(jwt);
        when(userRepo.findByEmailOrThrow(email)).thenReturn(user);
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
