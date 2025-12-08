package app.omniOne.service;

import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final CoachRepo coachRepo;
    private final ClientRepo clientRepo;
    private final ClientMapper clientMapper;

    public List<Client> getClients(UUID coachId) {
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        List<Client> clients = coach.getClients();
        log.info("Successfully retrieved Clients from Coach {}", coachId);
        return clients;
    }

    public Client getClient(UUID clientId) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        log.info("Successfully retrieved Client {}", clientId);
        return client;
    }

    public Client patchClient(UUID clientId, ClientPatchRequest request) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        clientMapper.map(request, client);
        Client savedClient = clientRepo.save(client);
        log.info("Successfully updated Client {}", clientId);
        return savedClient;
    }

}
