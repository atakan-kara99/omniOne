package app.omniOne.services;

import app.omniOne.exceptions.DuplicateResourceException;
import app.omniOne.exceptions.NoSuchResourceException;
import app.omniOne.models.entities.Coach;
import app.omniOne.repositories.CoachRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CoachService {

    private final CoachRepository coachRepository;

    public CoachService(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    public Coach getCoach(Long coachId) {
        Optional<Coach> coach = coachRepository.findById(coachId);
        if (coach.isEmpty())
            throw new NoSuchResourceException("Coach %d not found".formatted(coachId));
        return coach.get();
    }

    public Coach registerCoach(Coach coach) {
        String email = coach.getEmail();
        if (coachRepository.existsByEmail(email))
            throw new DuplicateResourceException("Coach already exists with email: %s".formatted(email));
        return coachRepository.save(coach);
    }

    public Coach updateCoach(Long coachId, Coach coach) {
        String email = coach.getEmail();
        if (coachRepository.existsByEmail(email))
            throw new DuplicateResourceException("Coach already exists with email: %s".formatted(email));
        coach.setId(coachId);
        return coachRepository.save(coach);
    }

    public void deleteCoach(Long coachId) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
        coach.getClients().forEach(c -> c.setCoach(null));
        coachRepository.delete(coach);
    }

}
