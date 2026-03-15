package app.omniOne.authentication;

import app.omniOne.authentication.token.JwtFilter;
import app.omniOne.configuration.SecurityConfig;
import app.omniOne.controller.coach.CoachClientController;
import app.omniOne.exception.ProblemDetailAccessDeniedHandler;
import app.omniOne.exception.ProblemDetailAuthenticationEntryPoint;
import app.omniOne.exception.ProblemDetailFactory;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import app.omniOne.service.CoachingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import({SecurityConfig.class, ProblemDetailFactory.class, ProblemDetailAuthenticationEntryPoint.class,
        ProblemDetailAccessDeniedHandler.class})
@WebMvcTest(CoachClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class MethodSecurityTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean(name = "authService") private AuthService authService;
    @MockitoBean private ClientMapper clientMapper;
    @MockitoBean private ClientService clientService;
    @MockitoBean private CoachingService coachingService;

    @Test void getClient_forbiddenWhenNotCoached() throws Exception {
        UUID clientId = UUID.randomUUID();
        when(authService.isCoachedByMe(clientId)).thenReturn(false);

        mockMvc.perform(get("/coach/clients/{clientId}", clientId))
                .andExpect(status().isForbidden());

        verify(authService).isCoachedByMe(clientId);
        verifyNoInteractions(clientService, clientMapper);
    }

}
