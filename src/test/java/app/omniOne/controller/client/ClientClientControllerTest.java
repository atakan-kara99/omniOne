package app.omniOne.controller.client;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.jwt.JwtFilter;
import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.dto.ClientResponse;
import app.omniOne.model.entity.Client;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ClientClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientClientControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private ClientMapper clientMapper;
    @MockitoBean private ClientService clientService;

    private UUID clientId;

    @BeforeEach void setUp() {
        clientId = UUID.randomUUID();
        mockAuthenticatedUser(clientId);
    }

    @Test void getClient_returnsMappedResponse() throws Exception {
        Client client = new Client();
        client.setId(clientId);
        ClientResponse response = new ClientResponse(clientId);
        when(clientService.getClient(clientId)).thenReturn(client);
        when(clientMapper.map(client)).thenReturn(response);

        mockMvc.perform(get("/client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId.toString()));

        verify(clientService).getClient(clientId);
        verify(clientMapper).map(client);
    }

    @Test void patchClient_updatesAndReturnsResponse() throws Exception {
        ClientPatchRequest request = new ClientPatchRequest();
        Client client = new Client();
        client.setId(clientId);
        ClientResponse response = new ClientResponse(clientId);

        when(clientService.patchClient(eq(clientId), any(ClientPatchRequest.class))).thenReturn(client);
        when(clientMapper.map(client)).thenReturn(response);

        mockMvc.perform(patch("/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId.toString()));

        verify(clientService).patchClient(eq(clientId), any(ClientPatchRequest.class));
        verify(clientMapper).map(client);
    }
}
