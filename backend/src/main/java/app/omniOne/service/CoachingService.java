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
        Client client = clientRepo.findByIdOrThrow(clientId);
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        client.setCoach(coach);
        Coaching coaching = Coaching.builder().coach(coach).client(client).build();
        coachingRepo.save(coaching);
        clientRepo.save(client);
        log.info("Coaching started (coachId={}, clientId={})", coachId, clientId);
    }

    @Transactional
    public void endCoaching(UUID clientId) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        Coach coach = client.getCoachOrThrow();
        client.setCoach(null);
        Coaching coaching = coachingRepo.findByCoachIdAndClientIdOrThrow(coach.getId(), clientId);
        coaching.setEndDate(LocalDateTime.now());
        log.info("Coaching ended (coachId={}, clientId={})", coach.getId(), clientId);
    }

}
