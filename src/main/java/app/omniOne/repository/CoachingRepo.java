package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.Coaching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoachingRepo extends JpaRepository<Coaching, Long> {

    boolean existsByCoachIdAndClientId(UUID coachId, UUID clientId);

    boolean existsByCoachIdAndClientIdAndEndDateIsNull(UUID coachId, UUID clientId);

    boolean existsByClientIdAndEndDateIsNull(UUID clientId);

    List<Coaching> findAllByCoachIdAndEndDateIsNull(UUID coachId);

    Optional<Coaching> findByClientIdAndEndDateIsNull(UUID clientId);

    default Coaching findByClientIdAndEndDateIsNullOrThrow(UUID clientId) {
        return findByClientIdAndEndDateIsNull(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Active coaching not found"));
    }

    default List<Coaching> findAllByCoachIdAndEndDateIsNullOrThrow(UUID coachId) {
        List<Coaching> coachings = findAllByCoachIdAndEndDateIsNull(coachId);
        if (coachings.isEmpty())
            throw new ResourceNotFoundException("Active coaching not found");
        return coachings;
    }

}
