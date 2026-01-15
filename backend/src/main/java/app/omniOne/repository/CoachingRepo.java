package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Coaching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoachingRepo extends JpaRepository<Coaching, Long> {

    boolean existsByCoachIdAndClientId(UUID coachId, UUID clientId);

    Optional<Coaching> findByCoachIdAndClientId(UUID coachId, UUID clientId);

    default Coaching findByCoachIdAndClientIdOrThrow(UUID coachId, UUID clientId) {
        return findByCoachIdAndClientId(coachId, clientId)
                .orElseThrow(() -> new NoSuchResourceException("Coaching not found"));
    }

}
