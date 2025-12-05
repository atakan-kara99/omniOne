package app.omniOne.service;

import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.dto.CoachPatchDto;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.repo.CoachRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepo coachRepo;
    private final CoachMapper coachMapper;

    public Coach getCoach(UUID coachId) {
        Optional<Coach> coach = coachRepo.findById(coachId);
        if (coach.isEmpty())
            throw new NoSuchResourceException("Coach %s not found".formatted(coachId));
        return coach.get();
    }

    public Coach patchCoach(UUID coachId, CoachPatchDto dto) {
        String email = dto.email();
        Coach coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new DuplicateResourceException("Coach already exists with email: %s".formatted(email)));
        coachMapper.map(dto, coach);
        return coachRepo.save(coach);
    }

    public void deleteCoach(UUID coachId) {
        Coach coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach %s not found".formatted(coachId)));
        coach.getClients().forEach(c -> c.setCoach(null));
        coachRepo.delete(coach);
    }

}
