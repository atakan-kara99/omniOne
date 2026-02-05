package app.omniOne.controller.coach;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.AuthService;
import app.omniOne.authentication.token.JwtFilter;
import app.omniOne.exception.ProblemDetailFactory;
import app.omniOne.service.ClientService;
import app.omniOne.service.CoachingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CoachClientController.class)
@Import(ProblemDetailFactory.class)
class CoachClientControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private AuthService authService;
    @MockitoBean private ClientService clientService;
    @MockitoBean private CoachingService coachingService;

    private UUID coachId;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        mockAuthenticatedUser(coachId);
    }

    @Test void invite_sendsInvitationMail() throws Exception {
        mockMvc.perform(post("/coach/clients/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InvitePayload("client@omni.one"))))
                .andExpect(status().isNoContent());

        verify(authService).sendInvitationMail("client@omni.one", coachId);
    }

    @Test void invite_returnsBadRequestOnValidationError() throws Exception {
        mockMvc.perform(post("/coach/clients/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InvitePayload(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(authService);
    }

    private record InvitePayload(String email) {}
}
