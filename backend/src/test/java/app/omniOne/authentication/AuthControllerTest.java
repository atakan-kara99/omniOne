package app.omniOne.authentication;

import app.omniOne.authentication.model.AuthMapper;
import app.omniOne.authentication.model.dto.*;
import app.omniOne.authentication.token.JwtFilter;
import app.omniOne.authentication.token.RefreshTokenProps;
import app.omniOne.model.entity.User;
import app.omniOne.model.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import app.omniOne.exception.ProblemDetailFactory;

import java.time.LocalDateTime;
import java.util.UUID;

import static app.omniOne.TestFixtures.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@Import(ProblemDetailFactory.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private AuthMapper authMapper;
    @MockitoBean private AuthService authService;
    @MockitoBean private RefreshTokenProps refreshTokenProps;

    @BeforeEach void setup() {
        when(refreshTokenProps.ttlDays()).thenReturn(30);
    }

    @Test void login_returnsJwtResponse() throws Exception {
        LoginRequest request = new LoginRequest(userEmail, "Testpq12");
        UUID deviceId = UUID.randomUUID();
        when(authService.login(request, deviceId)).thenReturn(new LoginResponse("jwt-token", "refresh-token"));

        mockMvc.perform(post("/auth/account/login")
                        .header("X-Device-Id", deviceId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("jwt-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=")))
                .andExpect(header().string("X-Device-Id", deviceId.toString()));

        verify(authService).login(request, deviceId);
    }

    @Test void register_returnsMappedAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(coachEmail, "Testpq12");
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        AuthResponse response = new AuthResponse(savedUser.getId(), coachEmail, UserRole.COACH,
                LocalDateTime.of(2025, 1, 1, 12, 0), false);

        when(authService.registerCoach(any(RegisterRequest.class))).thenReturn(savedUser);
        when(authMapper.map(savedUser)).thenReturn(response);

        mockMvc.perform(post("/auth/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.email").value(coachEmail))
                .andExpect(jsonPath("$.role").value("COACH"));

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).registerCoach(captor.capture());
        assertEquals(coachEmail, captor.getValue().email());
        verify(authMapper).map(savedUser);
    }

    @Test void activate_mapsServiceResult() throws Exception {
        String token = "activation-token";
        User user = new User();
        user.setId(UUID.randomUUID());
        AuthResponse response = new AuthResponse(user.getId(), userEmail, UserRole.CLIENT,
                LocalDateTime.of(2025, 2, 2, 9, 0), true);

        when(authService.activate(token)).thenReturn(user);
        when(authMapper.map(user)).thenReturn(response);

        mockMvc.perform(post("/auth/account/activate").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.id").value(user.getId().toString()));

        verify(authService).activate(token);
        verify(authMapper).map(user);
    }

    @Test void resend_sendsActivationMail() throws Exception {
        mockMvc.perform(post("/auth/account/resend").param("email", userEmail))
                .andExpect(status().isNoContent());

        verify(authService).sendActivationMail(userEmail);
    }

    @Test void forgot_sendsForgotMail() throws Exception {
        mockMvc.perform(post("/auth/password/forgot").param("email", userEmail))
                .andExpect(status().isNoContent());

        verify(authService).sendForgotMail(userEmail);
    }

    @Test void reset_updatesPasswordAndReturnsAuthResponse() throws Exception {
        String token = "reset-token";
        PasswordRequest request = new PasswordRequest("Testpq12");
        User user = new User();
        user.setId(UUID.randomUUID());
        AuthResponse response = new AuthResponse(user.getId(), userEmail, UserRole.CLIENT,
                LocalDateTime.of(2025, 3, 3, 8, 30), true);

        when(authService.reset(eq(token), any(PasswordRequest.class))).thenReturn(user);
        when(authMapper.map(user)).thenReturn(response);

        mockMvc.perform(post("/auth/password/reset")
                        .with(csrf())
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(authService).reset(eq(token), any(PasswordRequest.class));
        verify(authMapper).map(user);
    }

    @Test void acceptInvitation_registersAndReturnsAuthResponse() throws Exception {
        String token = "invite-token";
        PasswordRequest request = new PasswordRequest("Testpq12");
        User user = new User();
        user.setId(UUID.randomUUID());
        AuthResponse response = new AuthResponse(user.getId(), clientEmail, UserRole.CLIENT,
                LocalDateTime.of(2025, 4, 4, 7, 0), true);

        when(authService.acceptInvitation(eq(token), any(PasswordRequest.class))).thenReturn(user);
        when(authMapper.map(user)).thenReturn(response);

        mockMvc.perform(post("/auth/invitation/accept").param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(clientEmail));

        verify(authService).acceptInvitation(eq(token), any(PasswordRequest.class));
        verify(authMapper).map(user);
    }

    @Test void validateInvitation_returnsValidationResponse() throws Exception {
        String token = "invite-token";
        InvitationResponse response = new InvitationResponse(userEmail, true);

        when(authService.validateInvitation(token)).thenReturn(response);

        mockMvc.perform(post("/auth/invitation/validate").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.requiresPassword").value(true));

        verify(authService).validateInvitation(token);
    }

    @Test void reset_returnsBadRequestWhenTokenBlank() throws Exception {
        PasswordRequest request = new PasswordRequest("Testpq12");

        mockMvc.perform(post("/auth/password/reset").param("token", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists());
    }
}
