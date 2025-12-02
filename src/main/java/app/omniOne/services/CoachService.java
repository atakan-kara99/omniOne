package app.omniOne.services;

import app.omniOne.exceptions.DuplicateResourceException;
import app.omniOne.exceptions.NoSuchResourceException;
import app.omniOne.models.dtos.CoachPatchDto;
import app.omniOne.models.dtos.CoachPostDto;
import app.omniOne.models.entities.Coach;
import app.omniOne.models.mappers.CoachMapper;
import app.omniOne.repositories.CoachRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachMapper coachMapper;

    public CoachService(CoachRepository coachRepository, CoachMapper coachMapper) {
        this.coachRepository = coachRepository;
        this.coachMapper = coachMapper;
    }

    public Coach getCoach(Long coachId) {
        Optional<Coach> coach = coachRepository.findById(coachId);
        if (coach.isEmpty())
            throw new NoSuchResourceException("Coach %d not found".formatted(coachId));
        return coach.get();
    }

    public Coach registerCoach(CoachPostDto postDto) {
        String email = postDto.email();
        if (coachRepository.existsByEmail(email))
            throw new DuplicateResourceException("Coach already exists with email: %s".formatted(email));
        return coachRepository.save(new Coach(email));
    }

    public Coach patchCoach(Long coachId, CoachPatchDto patchDto) {
        String email = patchDto.email();
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new DuplicateResourceException("Coach already exists with email: %s".formatted(email)));
        coachMapper.patch(patchDto, coach);
        return coachRepository.save(coach);
    }

    public void deleteCoach(Long coachId) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %d not found".formatted(coachId)));
        coach.getClients().forEach(c -> c.setCoach(null));
        coachRepository.delete(coach);
    }

}
