package app.omniOne.service;

import app.omniOne.model.dto.ClientPatchDto;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.repo.ClientRepo;
import app.omniOne.repo.CoachRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepo clientRepo;
    private final CoachRepo coachRepo;
    private final ClientMapper clientMapper;

    public List<Client> getClients(UUID coachId) {
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        return coach.getClients();
    }

    public Client getClient(UUID coachId, UUID clientId) {
        return clientRepo.findByIdAndCoachIdOrThrow(clientId, coachId);
    }

    public Client patchClient(UUID clientId, ClientPatchDto dto) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        clientMapper.map(dto, client);
        return clientRepo.save(client);
    }

    public Client getClient(UUID clientId) {
        return clientRepo.findByIdOrThrow(clientId);
    }

}
