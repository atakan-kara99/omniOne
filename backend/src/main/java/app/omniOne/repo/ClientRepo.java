package app.omniOne.repo;

import app.omniOne.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepo extends JpaRepository<Client, UUID> {

    Optional<Client> findByIdAndCoachId(UUID clientId, UUID coachId);

}
