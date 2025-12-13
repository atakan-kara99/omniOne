package app.omniOne.service;

import app.omniOne.model.dto.CoachPatchRequest;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.repository.CoachRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) class CoachServiceTest {

    @Mock private CoachRepo coachRepo;
    @Mock private CoachMapper coachMapper;
    @InjectMocks private CoachService coachService;

    private UUID coachId;
    private Coach coach;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        coach = new Coach();
        coach.setId(coachId);
    }

    @Test void getCoach_returnsCoachFromRepository() {
        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(coach);

        Coach result = coachService.getCoach(coachId);

        assertSame(coach, result);
        verify(coachRepo).findByIdOrThrow(coachId);
        verifyNoInteractions(coachMapper);
    }

    @Test void patchCoach_mapsAndSavesCoach() {
        CoachPatchRequest request = new CoachPatchRequest("new@mail.com");
        Coach savedCoach = new Coach();
        savedCoach.setId(coachId);

        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(coach);
        when(coachRepo.save(coach)).thenReturn(savedCoach);

        Coach result = coachService.patchCoach(coachId, request);

        assertSame(savedCoach, result);
        verify(coachRepo).findByIdOrThrow(coachId);
        verify(coachMapper).map(request, coach);
        verify(coachRepo).save(coach);
        verifyNoMoreInteractions(coachRepo);
    }
}
