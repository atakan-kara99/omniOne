package app.omniOne.service;

import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static app.omniOne.TestFixtures.client;
import static app.omniOne.TestFixtures.coach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class ClientServiceTest {

    @Mock private CoachRepo coachRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private ClientMapper clientMapper;
    @InjectMocks private ClientService clientService;

    private UUID coachId;
    private UUID clientId;
    private Coach coach;
    private Client client;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        coach = coach(coachId);
        client = client(clientId);
    }

    @Test void getClients_returnsClientsOfCoach() {
        List<Client> clients = List.of(new Client(), new Client());
        coach.setClients(clients);

        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(coach);

        List<Client> result = clientService.getClients(coachId);

        assertEquals(clients, result);
        verify(coachRepo).findByIdOrThrow(coachId);
        verifyNoInteractions(clientRepo, clientMapper);
    }

    @Test void getClient_returnsClientFromRepository() {
        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);

        Client result = clientService.getClient(clientId);

        assertSame(client, result);
        verify(clientRepo).findByIdOrThrow(clientId);
        verifyNoInteractions(coachRepo, clientMapper);
    }

    //TODO
    @Test void patchClient_mapsAndSavesUpdatedClient() {
        ClientPatchRequest request = new ClientPatchRequest();
        Client savedClient = new Client();
        savedClient.setId(clientId);

        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        when(clientRepo.save(client)).thenReturn(savedClient);

        Client result = clientService.patchClient(clientId, request);

        assertSame(savedClient, result);
        verify(clientRepo).findByIdOrThrow(clientId);
        verify(clientMapper).map(request, client);
        verify(clientRepo).save(client);
        verifyNoInteractions(coachRepo);
    }
}
