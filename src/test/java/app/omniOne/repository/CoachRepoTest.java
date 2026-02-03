package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static app.omniOne.TestFixtures.coachEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoachRepoTest extends RepositoryTestBase {

    @Autowired private CoachRepo coachRepo;

    private Coach coach;

    @BeforeEach void setUp() {
        coach = persistCoach(persistUser(coachEmail, UserRole.COACH));
        flushAndClear();
    }

    @Test void findByIdOrThrow_returnsCoachWhenPresent() {
        Coach result = coachRepo.findByIdOrThrow(coach.getId());

        assertEquals(coach.getId(), result.getId());
    }

    @Test void findByIdOrThrow_throwsWhenMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> coachRepo.findByIdOrThrow(UUID.randomUUID()));

        assertEquals("Coach not found", exception.getMessage());
    }
}
