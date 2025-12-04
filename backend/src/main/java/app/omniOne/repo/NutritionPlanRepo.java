package app.omniOne.repo;

import app.omniOne.model.entity.NutritionPlan;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionPlanRepo extends JpaRepository<NutritionPlan, Long> {

    Optional<NutritionPlan> findByClientIdAndClientCoachId(Long clientId, Long coachId);

    Optional<NutritionPlan> findByClientIdAndClientCoachIdAndEndDateIsNull(Long clientId, Long coachId);

    List<NutritionPlan> findByClientIdAndClientCoachId(Long clientId, Long coachId, Sort sort);

    Optional<NutritionPlan> findByClientIdAndEndDateIsNull(Long clientId);

    List<NutritionPlan> findByClientId(Long clientId, Sort sort);
}
