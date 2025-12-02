package app.omniOne.services;

import app.omniOne.exceptions.NoSuchResourceException;
import app.omniOne.models.entities.Client;
import app.omniOne.models.entities.NutritionPlan;
import app.omniOne.repositories.ClientRepository;
import app.omniOne.repositories.NutritionPlanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final ClientRepository clientRepository;

    public NutritionPlanService(NutritionPlanRepository nutritionPlanRepository,
                                ClientRepository clientRepository) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.clientRepository = clientRepository;
    }

    public NutritionPlan addNutritionPlan(Long coachId, Long clientId, NutritionPlan np) {
        Client client = clientRepository.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        Optional<NutritionPlan> activeNP = nutritionPlanRepository.findByClientIdAndClientCoachIdAndEndDateIsNull(clientId, coachId);
        if (activeNP.isPresent()) {
            activeNP.get().setEndDate(LocalDate.now());
            nutritionPlanRepository.save(activeNP.get());
        }
        np.setClient(client);
        np.setStartDate(LocalDate.now());
        np.setEndDate(null);
        return nutritionPlanRepository.save(np);
    }

    public NutritionPlan getNutritionPlan(Long coachId, Long clientId) {
        return nutritionPlanRepository.findByClientIdAndClientCoachIdAndEndDateIsNull(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d has no NutritionPlan".formatted(clientId)));
    }

    public List<NutritionPlan> getNutritionPlans(Long coachId, Long clientId) {
        Client client = clientRepository.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        List<NutritionPlan> nps = client.getNutritionPlans();
        if (nps == null)
            throw new NoSuchResourceException("Client %d has no NutritionPlan".formatted(clientId));
        return nps;
    }

}
