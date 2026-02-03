package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.Coaching;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static app.omniOne.TestFixtures.clientEmail;
import static app.omniOne.TestFixtures.coachEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoachingRepoTest extends RepositoryTestBase {

    @Autowired private CoachingRepo coachingRepo;

    private Coach coach;
    private Client client;

    @BeforeEach void setUp() {
        coach = persistCoach(persistUser(coachEmail, UserRole.COACH));
        client = persistClient(persistUser(clientEmail, UserRole.CLIENT), coach);
        persistCoaching(coach, client);
        flushAndClear();
    }

    @Test void findByCoachIdAndClientIdOrThrow_returnsCoachingWhenPresent() {
        Coaching result = coachingRepo.findByCoachIdAndClientIdOrThrow(coach.getId(), client.getId());

        assertEquals(coach.getId(), result.getCoach().getId());
        assertEquals(client.getId(), result.getClient().getId());
    }

    @Test void findByCoachIdAndClientIdOrThrow_throwsWhenMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> coachingRepo.findByCoachIdAndClientIdOrThrow(coach.getId(), UUID.randomUUID()));

        assertEquals("Coaching not found", exception.getMessage());
    }
}
