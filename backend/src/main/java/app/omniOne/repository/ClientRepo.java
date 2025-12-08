package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepo extends JpaRepository<Client, UUID> {

    default Client findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchResourceException("Client not found"));
    }

}
