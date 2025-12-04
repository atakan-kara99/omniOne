package app.omniOne.repo;

import app.omniOne.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long> {

    boolean existsByEmail(String email);

    Optional<Client> findByIdAndCoachId(Long clientId, Long coachId);

}
