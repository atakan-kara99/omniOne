package app.omniOne.repositories;

import app.omniOne.models.entities.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Long> {

    Optional<NutritionPlan> findByClientIdAndClientCoachId(Long clientId, Long coachId);

    Optional<NutritionPlan> findByClientIdAndClientCoachIdAndEndDateIsNull(Long clientId, Long coachId);

}
