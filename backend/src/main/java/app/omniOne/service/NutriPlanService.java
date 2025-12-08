package app.omniOne.service;

import app.omniOne.model.dto.NutriPlanPostRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.NutriPlan;
import app.omniOne.model.mapper.NutriPlanMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.NutriPlanRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutriPlanService {

    private final ClientRepo clientRepo;
    private final NutriPlanRepo nutriPlanRepo;
    private final NutriPlanMapper nutriPlanMapper;

    public NutriPlan addNutriPlan(UUID clientId, NutriPlanPostRequest request) {
        log.debug("Trying to add NutritionPlan for Client {}", clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        NutriPlan nutriPlan = new NutriPlan();
        nutriPlanMapper.map(request, nutriPlan);
        nutriPlan.setClient(client);
        NutriPlan savedNutriPlan = nutriPlanRepo.save(nutriPlan);
        log.info("Successfully added NutritionPlan");
        return savedNutriPlan;
    }

    public NutriPlan getActiveNutriPlan(UUID clientId) {
        log.debug("Trying to retrieve active NutritionPlan for Client {}", clientId);
        NutriPlan nutriPlan = nutriPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(clientId);
        log.info("Successfully retrieved active NutritionPlan");
        return nutriPlan;
    }

    public List<NutriPlan> getNutriPlans(UUID clientId) {
        log.debug("Trying to retrieve NutritionPlans for Client {}", clientId);
        clientRepo.findByIdOrThrow(clientId);
        List<NutriPlan> nutriPlans = nutriPlanRepo.findByClientIdOrderByCreatedAtDescOrThrow(clientId);
        log.info("Successfully retrieved NutritionPlans");
        return nutriPlans;
    }

}
