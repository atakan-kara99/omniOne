package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.NutriPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NutriPlanRepo extends JpaRepository<NutriPlan, Long> {

    Optional<NutriPlan> findFirstByClientIdOrderByCreatedAtDesc(UUID clientId);

    default NutriPlan findFirstByClientIdOrderByCreatedAtDescOrThrow(UUID clientId) {
        return findFirstByClientIdOrderByCreatedAtDesc(clientId)
                .orElseThrow(() -> new NoSuchResourceException("NutriPlan not found"));
    }

    Optional<List<NutriPlan>> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    default List<NutriPlan> findByClientIdOrderByCreatedAtDescOrThrow(UUID clientId) {
        return findByClientIdOrderByCreatedAtDesc(clientId)
                .orElseThrow(() -> new NoSuchResourceException("NutriPlans not found"));
    }

}
