package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class CoachRepoTest extends RepositoryTestBase {

    @Autowired private CoachRepo coachRepo;

    private Coach coach;

    @BeforeEach void setUp() {
        coach = persistCoach(persistUser("coach@omni.one", UserRole.COACH));
        flushAndClear();
    }

    @Test void findByIdOrThrow_returnsCoachWhenPresent() {
        Coach result = coachRepo.findByIdOrThrow(coach.getId());

        assertEquals(coach.getId(), result.getId());
    }

    @Test void findByIdOrThrow_throwsWhenMissing() {
        NoSuchResourceException exception = assertThrows(
                NoSuchResourceException.class,
                () -> coachRepo.findByIdOrThrow(UUID.randomUUID()));

        assertEquals("Coach not found", exception.getMessage());
    }
}
