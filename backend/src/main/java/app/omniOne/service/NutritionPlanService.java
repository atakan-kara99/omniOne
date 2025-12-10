package app.omniOne.service;

import app.omniOne.model.dto.NutritionPlanRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.NutritionPlanRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionPlanService {

    private final ClientRepo clientRepo;
    private final NutritionPlanRepo nutritionPlanRepo;
    private final NutritionPlanMapper nutritionPlanMapper;

    public NutritionPlan addNutriPlan(UUID clientId, NutritionPlanRequest request) {
        log.debug("Trying to add NutritionPlan for Client {}", clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        NutritionPlan nutritionPlan = new NutritionPlan();
        nutritionPlanMapper.map(request, nutritionPlan);
        nutritionPlan.setClient(client);
        NutritionPlan savedNutritionPlan = nutritionPlanRepo.save(nutritionPlan);
        log.info("Successfully added NutritionPlan");
        return savedNutritionPlan;
    }

    public NutritionPlan getActiveNutriPlan(UUID clientId) {
        log.debug("Trying to retrieve active NutritionPlan for Client {}", clientId);
        NutritionPlan nutritionPlan = nutritionPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(clientId);
        log.info("Successfully retrieved active NutritionPlan");
        return nutritionPlan;
    }

    public List<NutritionPlan> getNutriPlans(UUID clientId) {
        log.debug("Trying to retrieve NutritionPlans for Client {}", clientId);
        clientRepo.findByIdOrThrow(clientId);
        List<NutritionPlan> nutritionPlans = nutritionPlanRepo.findByClientIdOrderByCreatedAtDescOrThrow(clientId);
        log.info("Successfully retrieved NutritionPlans");
        return nutritionPlans;
    }

    public NutritionPlan correctNutriPlan(UUID clientId, Long nutriPlanId, NutritionPlanRequest request) {
        log.debug("Trying to correct NutritionPlan {} for Client {}", nutriPlanId, clientId);
        clientRepo.findByIdOrThrow(clientId);
        NutritionPlan nutritionPlan = nutritionPlanRepo.findByIdAndClientIdOrThrow(nutriPlanId, clientId);
        nutritionPlanMapper.map(request, nutritionPlan);
        NutritionPlan savedNutritionPlan = nutritionPlanRepo.save(nutritionPlan);
        log.info("Successfully corrected NutritionPlan");
        return savedNutritionPlan;
    }

}
