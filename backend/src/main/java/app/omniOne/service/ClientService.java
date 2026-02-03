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
import app.omniOne.repository.UserProfileRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    private final UserProfileRepo userProfileRepo;

    private final ClientRepo clientRepo;
    private final CoachMapper coachMapper;
    private final ClientMapper clientMapper;

    public List<ClientResponse> getClients(UUID coachId) {
        return clientRepo.findClientsByCoachId(coachId);
    }

    public ClientResponse getClient(UUID clientId) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        UserProfile profile = userProfileRepo.findByIdOrThrow(clientId);
        return clientMapper.map(client, profile);
    }

    public Client patchClient(UUID clientId, ClientPatchRequest request) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        clientMapper.map(request, client);
        Client savedClient = clientRepo.save(client);
        log.info("Client updated (clientId={})", savedClient.getId());
        return savedClient;
    }

    public CoachResponse getCoach(UUID clientId) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        Coach coach = client.getCoachOrThrow();
        UserProfile profile = userProfileRepo.findByIdOrThrow(coach.getId());
        return coachMapper.map(coach, profile);
    }

}
