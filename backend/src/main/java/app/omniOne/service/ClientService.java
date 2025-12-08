package app.omniOne.service;

import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final CoachRepo coachRepo;
    private final ClientRepo clientRepo;
    private final ClientMapper clientMapper;

    public List<Client> getClients(UUID coachId) {
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        return coach.getClients();
    }

    public Client getClient(UUID clientId) {
        return clientRepo.findByIdOrThrow(clientId);
    }

    public Client patchClient(UUID clientId, ClientPatchRequest request) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        clientMapper.map(request, client);
        return clientRepo.save(client);
    }

}
