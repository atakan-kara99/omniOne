package app.omniOne.service;

import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.dto.ClientPatchDto;
import app.omniOne.model.dto.ClientPostDto;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.repo.ClientRepo;
import app.omniOne.repo.CoachRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepo clientRepo;
    private final CoachRepo coachRepo;
    private final ClientMapper clientMapper;

    public Client registerClient(Long coachId, ClientPostDto dto) {
        String email = dto.email();
        if (clientRepo.existsByEmail(email))
            throw new DuplicateResourceException("Client already exists with email: %s".formatted(email));
        Coach coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
        Client newClient = new Client(email, coach);
        return clientRepo.save(newClient);
    }

    public List<Client> getClients(Long coachId) {
        Coach coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
        return coach.getClients();
    }

    public Client getClient(Long coachId, Long clientId) {
        return clientRepo.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
    }

    public Client patchClient(Long clientId, ClientPatchDto dto) {
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        clientMapper.map(dto, client);
        return clientRepo.save(client);
    }

    public Client getClient(Long clientId) {
        return clientRepo.findById(clientId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
    }

}
