package app.omniOne.controller.client;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.jwt.JwtFilter;
import app.omniOne.service.CoachingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ClientCoachController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientCoachControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ClientCoachController controller;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private CoachingService coachingService;

    private UUID clientId;

    @BeforeEach void setUp() {
        clientId = UUID.randomUUID();
        mockAuthenticatedUser(clientId);
        ReflectionTestUtils.setField(controller, "coachingService", coachingService);
    }

    @Test void endCoaching_triggersService() throws Exception {
        mockMvc.perform(delete("/client/coach"))
                .andExpect(status().isNoContent());

        verify(coachingService).endCoaching(clientId);
    }
}
