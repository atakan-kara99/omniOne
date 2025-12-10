package app.omniOne.service;

import app.omniOne.model.dto.CoachPatchRequest;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.repository.CoachRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepo coachRepo;
    private final CoachMapper coachMapper;

    public Coach getCoach(UUID coachId) {
        log.debug("Trying to retrieve Coach {}", coachId);
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        log.info("Successfully retrieved Coach");
        return coach;
    }

    public Coach patchCoach(UUID coachId, CoachPatchRequest request) {
        log.debug("Trying to update Coach {}", coachId);
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        coachMapper.map(request, coach);
        Coach savedCoach = coachRepo.save(coach);
        log.info("Successfully updated Coach");
        return savedCoach;
    }

}
