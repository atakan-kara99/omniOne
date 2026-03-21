package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.SupplementPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplementPlanRepo extends JpaRepository<SupplementPlan, Long> {

    Optional<SupplementPlan> findByIdAndClientId(Long id, UUID clientId);

    default SupplementPlan findByIdAndClientIdOrThrow(Long id, UUID clientId) {
        return findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("SupplementPlan not found"));
    }

    Optional<SupplementPlan> findFirstByClientIdOrderByCreatedAtDesc(UUID clientId);

    default SupplementPlan findFirstByClientIdOrderByCreatedAtDescOrThrow(UUID clientId) {
        return findFirstByClientIdOrderByCreatedAtDesc(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("No supplement plan found"));
    }

    List<SupplementPlan> findByClientIdOrderByCreatedAtDesc(UUID clientId);

}
