package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static app.omniOne.TestFixtures.clientEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientRepoTest extends RepositoryTestBase {

    @Autowired private ClientRepo clientRepo;

    private Client client;

    @BeforeEach void setUp() {
        client = persistClient(persistUser(clientEmail, UserRole.CLIENT), null);
        flushAndClear();
    }

    @Test void findByIdOrThrow_returnsClientWhenPresent() {
        Client result = clientRepo.findByIdOrThrow(client.getId());

        assertEquals(client.getId(), result.getId());
    }

    @Test void findByIdOrThrow_throwsWhenMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clientRepo.findByIdOrThrow(UUID.randomUUID()));

        assertEquals("Client not found", exception.getMessage());
    }
}
