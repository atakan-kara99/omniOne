package app.omniOne.repo;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.NutritionPlan;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NutritionPlanRepo extends JpaRepository<NutritionPlan, Long> {

    Optional<NutritionPlan> findByClientIdAndClientCoachId(UUID clientId, UUID coachId);

    Optional<NutritionPlan> findByClientIdAndClientCoachIdAndEndDateIsNull(UUID clientId, UUID coachId);

    List<NutritionPlan> findByClientIdAndClientCoachId(UUID clientId, UUID coachId, Sort sort);

    Optional<NutritionPlan> findByClientIdAndEndDateIsNull(UUID clientId);

    List<NutritionPlan> findByClientId(UUID clientId, Sort sort);

    default NutritionPlan findByClientIdAndEndDateIsNullOrThrow(UUID clientId) {
        return findByClientIdAndEndDateIsNull(clientId)
                .orElseThrow(() -> new NoSuchResourceException("NutritionPlan not found"));
    }

    default NutritionPlan findByClientIdAndClientCoachIdAndEndDateIsNullOrThrow(UUID clientId, UUID coachId) {
        return findByClientIdAndClientCoachIdAndEndDateIsNull(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("NutritionPlan not found"));
    }

}
