package app.omniOne.services;

import app.omniOne.exceptions.DuplicateResourceException;
import app.omniOne.exceptions.NoSuchResourceException;
import app.omniOne.models.entities.Client;
import app.omniOne.models.entities.Coach;
import app.omniOne.models.enums.ClientStatus;
import app.omniOne.repositories.ClientRepository;
import app.omniOne.repositories.CoachRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final CoachRepository coachRepository;

    public ClientService(ClientRepository clientRepository, CoachRepository coachRepository) {
        this.clientRepository = clientRepository;
        this.coachRepository = coachRepository;
    }

    public Client registerClient(Long coachId, Client client) {
        String email = client.getEmail();
        if (clientRepository.existsByEmail(email))
            throw new DuplicateResourceException("Client already exists with email: %s".formatted(email));
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
        Client newClient = new Client();
        newClient.setEmail(email);
        newClient.setStatus(ClientStatus.PENDING);
        newClient.setCoach(coach);
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

    public Client updateClient(Long clientId, Client newClient) {
        if (!clientRepository.existsById(clientId))
            throw new NoSuchResourceException("Client %d not found".formatted(clientId));
        newClient.setId(clientId);
        return clientRepository.save(newClient);
    }

}
