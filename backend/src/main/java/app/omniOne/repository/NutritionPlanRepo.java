package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NutritionPlanRepo extends JpaRepository<NutritionPlan, Long> {

    Optional<NutritionPlan> findByIdAndClientId(Long id, UUID clientId);

    default NutritionPlan findByIdAndClientIdOrThrow(Long id, UUID clientId) {
        return findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan not found"));
    }

    Optional<NutritionPlan> findFirstByClientIdOrderByCreatedAtDesc(UUID clientId);

    default NutritionPlan findFirstByClientIdOrderByCreatedAtDescOrThrow(UUID clientId) {
        return findFirstByClientIdOrderByCreatedAtDesc(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("NutritionPlan not found"));
    }

    List<NutritionPlan> findByClientIdOrderByCreatedAtDesc(UUID clientId);

}
