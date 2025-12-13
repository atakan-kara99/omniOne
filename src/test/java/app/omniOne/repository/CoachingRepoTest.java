package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.Coaching;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class CoachingRepoTest extends RepositoryTestBase {

    @Autowired private CoachingRepo coachingRepo;

    private Coach coach;
    private Client client;

    @BeforeEach void setUp() {
        coach = persistCoach(persistUser("coach@omni.one", UserRole.COACH));
        client = persistClient(persistUser("client@omni.one", UserRole.CLIENT), coach);
        persistCoaching(coach, client);
        flushAndClear();
    }

    @Test void findByCoachIdAndClientIdOrThrow_returnsCoachingWhenPresent() {
        Coaching result = coachingRepo.findByCoachIdAndClientIdOrThrow(coach.getId(), client.getId());

        assertEquals(coach.getId(), result.getCoach().getId());
        assertEquals(client.getId(), result.getClient().getId());
    }

    @Test void findByCoachIdAndClientIdOrThrow_throwsWhenMissing() {
        NoSuchResourceException exception = assertThrows(
                NoSuchResourceException.class,
                () -> coachingRepo.findByCoachIdAndClientIdOrThrow(coach.getId(), UUID.randomUUID()));

        assertEquals("Coaching not found", exception.getMessage());
    }
}
