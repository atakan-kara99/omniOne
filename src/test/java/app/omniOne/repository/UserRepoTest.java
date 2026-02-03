package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.User;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static app.omniOne.TestFixtures.userEmail;
import static org.junit.jupiter.api.Assertions.*;

class UserRepoTest extends RepositoryTestBase {

    @Autowired private UserRepo userRepo;

    private User user;

    @BeforeEach void setUp() {
        user = persistUser(userEmail, UserRole.ADMIN);
        flushAndClear();
    }

    @Test void existsByEmail_matchesPersistedUser() {
        boolean exists = userRepo.existsByEmail(userEmail);
        boolean missing = userRepo.existsByEmail("missing@omni.one");

        assertTrue(exists);
        assertFalse(missing);
    }

    @Test void findByEmail_returnsOptionalWhenPresent() {
        Optional<User> result = userRepo.findByEmail(userEmail);

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
    }

    @Test void findByEmail_returnsEmptyWhenMissing() {
        Optional<User> result = userRepo.findByEmail("missing@omni.one");

        assertTrue(result.isEmpty());
    }

    @Test void findByIdOrThrow_returnsUserWhenPresent() {
        User result = userRepo.findByIdOrThrow(user.getId());

        assertEquals(user.getId(), result.getId());
    }

    @Test void findByIdOrThrow_throwsWhenMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userRepo.findByIdOrThrow(UUID.randomUUID()));

        assertEquals("User not found", exception.getMessage());
    }

    @Test void findByEmailOrThrow_returnsUserWhenPresent() {
        User result = userRepo.findByEmailOrThrow(userEmail);

        assertEquals(user.getId(), result.getId());
    }

    @Test void findByEmailOrThrow_throwsWhenMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userRepo.findByEmailOrThrow("missing@omni.one"));

        assertEquals("User not found", exception.getMessage());
    }
}
