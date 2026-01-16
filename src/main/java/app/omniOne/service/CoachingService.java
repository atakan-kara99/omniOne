package app.omniOne.service;

import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.Coaching;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.CoachingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoachingService {

    private final CoachRepo coachRepo;
    private final ClientRepo clientRepo;
    private final CoachingRepo coachingRepo;

    @Transactional
    public void startCoaching(UUID coachId, UUID clientId) {
        log.debug("Trying to start coaching by Coach {} for Client {}", coachId, clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        client.setCoach(coach);
        Coaching coaching = Coaching.builder().coach(coach).client(client).build();
        coachingRepo.save(coaching);
        clientRepo.save(client);
        log.info("Successfully started coaching");
    }

    @Transactional
    public void endCoaching(UUID clientId) {
        log.debug("Trying to end coaching for Client {}", clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        UUID coachId = client.getCoach().getId();
        client.setCoach(null);
        Coaching coaching = coachingRepo.findByCoachIdAndClientIdOrThrow(coachId, clientId);
        coaching.setEndDate(LocalDateTime.now());
        log.info("Successfully ended coaching");
    }

}
