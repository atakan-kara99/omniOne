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
        Client client = clientRepo.findByIdOrThrow(clientId);
        NutritionPlan nutritionPlan = new NutritionPlan();
        nutritionPlanMapper.map(request, nutritionPlan);
        nutritionPlan.setClient(client);
        NutritionPlan savedNutritionPlan = nutritionPlanRepo.save(nutritionPlan);
        log.info("NutritionPlan added (clientId={}, planId={})", clientId, savedNutritionPlan.getId());
        return savedNutritionPlan;
    }

    public NutritionPlan getActiveNutriPlan(UUID clientId) {
        return nutritionPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(clientId);
    }

    public List<NutritionPlan> getNutriPlans(UUID clientId) {
        clientRepo.findByIdOrThrow(clientId);
        return nutritionPlanRepo.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public NutritionPlan correctNutriPlan(UUID clientId, Long nutriPlanId, NutritionPlanRequest request) {
        clientRepo.findByIdOrThrow(clientId);
        NutritionPlan nutritionPlan = nutritionPlanRepo.findByIdAndClientIdOrThrow(nutriPlanId, clientId);
        nutritionPlanMapper.map(request, nutritionPlan);
        NutritionPlan savedNutritionPlan = nutritionPlanRepo.save(nutritionPlan);
        log.info("NutritionPlan corrected (clientId={}, planId={})", clientId, savedNutritionPlan.getId());
        return savedNutritionPlan;
    }

}
