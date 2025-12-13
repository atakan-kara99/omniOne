package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ClientRepoTest extends RepositoryTestBase {

    @Autowired private ClientRepo clientRepo;

    private Client client;

    @BeforeEach void setUp() {
        client = persistClient(persistUser("client@omni.one", UserRole.CLIENT), null);
        flushAndClear();
    }

    @Test void findByIdOrThrow_returnsClientWhenPresent() {
        Client result = clientRepo.findByIdOrThrow(client.getId());

        assertEquals(client.getId(), result.getId());
    }

    @Test void findByIdOrThrow_throwsWhenMissing() {
        NoSuchResourceException exception = assertThrows(
                NoSuchResourceException.class,
                () -> clientRepo.findByIdOrThrow(UUID.randomUUID()));

        assertEquals("Client not found", exception.getMessage());
    }
}
