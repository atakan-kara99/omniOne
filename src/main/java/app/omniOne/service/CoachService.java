package app.omniOne.service;

import app.omniOne.model.dto.CoachPatchRequest;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.repository.CoachRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepo coachRepo;
    private final CoachMapper coachMapper;

    public Coach getCoach(UUID coachId) {
        return coachRepo.findByIdOrThrow(coachId);
    }

    public Coach patchCoach(UUID coachId, CoachPatchRequest request) {
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        coachMapper.map(request, coach);
        return coachRepo.save(coach);
    }

    public void deleteCoach(UUID coachId) {
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        coach.getClients().forEach(c -> c.setCoach(null));
        coachRepo.delete(coach);
    }

}
