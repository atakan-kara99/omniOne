package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.NutriPlan;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NutriPlanRepo extends JpaRepository<NutriPlan, Long> {

    List<NutriPlan> findByClientId(UUID clientId, Sort sort);

    Optional<NutriPlan> findByClientIdAndEndDateIsNull(UUID clientId);

    default NutriPlan findByClientIdAndEndDateIsNullOrThrow(UUID clientId) {
        return findByClientIdAndEndDateIsNull(clientId)
                .orElseThrow(() -> new NoSuchResourceException("NutriPlan not found"));
    }

}
