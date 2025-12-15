package app.omniOne.controller.coach;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.AuthService;
import app.omniOne.authentication.jwt.JwtFilter;
import app.omniOne.model.dto.ClientResponse;
import app.omniOne.model.entity.Client;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import app.omniOne.service.CoachingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static app.omniOne.TestFixtures.client;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CoachClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class CoachClientControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private AuthService authService;
    @MockitoBean private ClientMapper clientMapper;
    @MockitoBean private ClientService clientService;
    @MockitoBean private CoachingService coachingService;

    private UUID coachId;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        mockAuthenticatedUser(coachId);
    }

    @Test void getClients_returnsMappedList() throws Exception {
        UUID clientId = UUID.randomUUID();
        Client client = client(clientId);
        ClientResponse response = new ClientResponse(clientId);

        when(clientService.getClients(coachId)).thenReturn(List.of(client));
        when(clientMapper.map(client)).thenReturn(response);

        mockMvc.perform(get("/coach/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(clientId.toString()));

        verify(clientService).getClients(coachId);
        verify(clientMapper).map(client);
    }

    @Test void getClient_returnsResponseWhenAuthorized() throws Exception {
        UUID clientId = UUID.randomUUID();
        Client client = client(clientId);
        ClientResponse response = new ClientResponse(clientId);

        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        when(clientService.getClient(clientId)).thenReturn(client);
        when(clientMapper.map(client)).thenReturn(response);

        mockMvc.perform(get("/coach/clients/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId.toString()));

        verify(clientService).getClient(clientId);
        verify(clientMapper).map(client);
    }

    @Test void invite_sendsInvitationMail() throws Exception {
        String email = "client@omni.one";

        mockMvc.perform(get("/coach/clients/invite").param("email", email))
                .andExpect(status().isNoContent());

        verify(authService).sendInvitationMail(email, coachId);
    }

    @Test void endCoaching_endsRelationship() throws Exception {
        UUID clientId = UUID.randomUUID();

        mockMvc.perform(delete("/coach/clients/{clientId}", clientId))
                .andExpect(status().isNoContent());

        verify(coachingService).endCoaching(clientId);
        verifyNoMoreInteractions(clientMapper);
    }
}
