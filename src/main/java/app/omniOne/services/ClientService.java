package app.omniOne.services;

import app.omniOne.exceptions.DuplicateResourceException;
import app.omniOne.exceptions.NoSuchResourceException;
import app.omniOne.models.dtos.ClientPatchDto;
import app.omniOne.models.dtos.ClientPostDto;
import app.omniOne.models.entities.Client;
import app.omniOne.models.entities.Coach;
import app.omniOne.models.mappers.ClientMapper;
import app.omniOne.repositories.ClientRepository;
import app.omniOne.repositories.CoachRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final CoachRepository coachRepository;
    private final ClientMapper clientMapper;

    public ClientService(
            ClientRepository clientRepository, CoachRepository coachRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.coachRepository = coachRepository;
        this.clientMapper = clientMapper;
    }

    public Client registerClient(Long coachId, ClientPostDto postDto) {
        String email = postDto.email();
        if (clientRepository.existsByEmail(email))
            throw new DuplicateResourceException("Client already exists with email: %s".formatted(email));
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
        Client newClient = new Client(email, coach);
        return clientRepository.save(newClient);
    }

    public List<Client> getClients(Long coachId) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
        return coach.getClients();
    }

    public Client getClient(Long coachId, Long clientId) {
        return clientRepository.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
    }

    public Client patchClient(Long clientId, ClientPatchDto patchDto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        clientMapper.patch(patchDto, client);
        return clientRepository.save(client);
    }

    public Client getClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
    }

}
