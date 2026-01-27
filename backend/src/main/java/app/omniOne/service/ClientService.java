package app.omniOne.service;

import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.dto.ClientResponse;
import app.omniOne.model.dto.CoachResponse;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final UserRepo userRepo;
    private final ClientRepo clientRepo;
    private final CoachMapper coachMapper;
    private final ClientMapper clientMapper;

    public List<ClientResponse> getClients(UUID coachId) {
        log.debug("Trying to retrieve Clients from Coach {}", coachId);
        List<ClientResponse> clientResponses = clientRepo.findClientsByCoachId(coachId);
        log.info("Successfully retrieved Clients");
        return clientResponses;
    }

    public ClientResponse getClient(UUID clientId) {
        log.debug("Trying to retrieve Client {}", clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        UserProfile profile = userRepo.findByIdOrThrow(clientId).getProfile();
        ClientResponse clientResponse = clientMapper.map(client, profile);
        log.info("Successfully retrieved Client");
        return clientResponse;
    }

    public Client patchClient(UUID clientId, ClientPatchRequest request) {
        log.debug("Trying to update Client {}", clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        clientMapper.map(request, client);
        Client savedClient = clientRepo.save(client);
        log.info("Successfully updated Client");
        return savedClient;
    }

    public CoachResponse getCoach(UUID clientId) {
        Coach coach = clientRepo.findByIdOrThrow(clientId).getCoach();
        UserProfile profile = userRepo.findByIdOrThrow(coach.getId()).getProfile();
        return coachMapper.map(coach, profile);
    }

}
