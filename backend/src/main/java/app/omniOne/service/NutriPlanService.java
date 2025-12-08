package app.omniOne.service;

import app.omniOne.model.dto.NutriPlanPostRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.NutriPlan;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.NutriPlanRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutriPlanService {

    private final ClientRepo clientRepo;
    private final NutriPlanRepo nutriPlanRepo;

    @Transactional
    public NutriPlan addNutriPlan(UUID clientId, NutriPlanPostRequest request) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        nutriPlanRepo.findByClientIdAndEndDateIsNull(clientId)
                .ifPresent(activeNP -> activeNP.setEndDate(LocalDate.now()));
        NutriPlan newPlan = new NutriPlan(
                request.carbohydrates(),
                request.proteins(),
                request.fats(),
                client
        );
        NutriPlan savedNutriPlan = nutriPlanRepo.save(newPlan);
        log.info("Successfully added NutritionPlan for Client {}", clientId);
        return savedNutriPlan;
    }

    public NutriPlan getActiveNutriPlan(UUID clientId) {
        NutriPlan nutriPlan = nutriPlanRepo.findByClientIdAndEndDateIsNullOrThrow(clientId);
        log.info("Successfully retrieved active NutritionPlan for Client {}", clientId);
        return nutriPlan;
    }

    public List<NutriPlan> getNutriPlans(UUID clientId) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        Sort sort = Sort.by(Sort.Direction.ASC, "startDate");
        List<NutriPlan> nutriPlans = nutriPlanRepo.findByClientId(clientId, sort);
        log.info("Successfully retrieved NutritionPlans for Client {}", clientId);
        return nutriPlans;
    }
}
