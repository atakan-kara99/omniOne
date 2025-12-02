package app.omniOne.repositories;

import app.omniOne.models.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByEmail(String email);

    Optional<Client> findByIdAndCoachId(Long clientId, Long coachId);

}
