package app.omniOne.service;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.dto.NutritionPlanPostDto;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.repo.ClientRepo;
import app.omniOne.repo.NutritionPlanRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NutritionPlanService {

    private final NutritionPlanRepo nutritionPlanRepo;
    private final ClientRepo clientRepo;

    @Transactional
    public NutritionPlan addNutritionPlan(UUID coachId, UUID clientId, NutritionPlanPostDto dto) {
        Client client = clientRepo.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        nutritionPlanRepo.findByClientIdAndClientCoachIdAndEndDateIsNull(clientId, coachId)
                .ifPresent(activeNP -> activeNP.setEndDate(LocalDate.now()));
        NutritionPlan newPlan = new NutritionPlan(
                dto.carbohydrates(),
                dto.proteins(),
                dto.fats(),
                client
        );
        return nutritionPlanRepo.save(newPlan);
    }

    public NutritionPlan getActiveNutritionPlan(UUID coachId, UUID clientId) {
        return nutritionPlanRepo.findByClientIdAndClientCoachIdAndEndDateIsNull(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d has no NutritionPlan".formatted(clientId)));
    }

    public NutritionPlan getActiveNutritionPlan(UUID clientId) {
        return nutritionPlanRepo.findByClientIdAndEndDateIsNull(clientId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d has no NutritionPlan".formatted(clientId)));
    }

    public List<NutritionPlan> getNutritionPlans(UUID coachId, UUID clientId) {
        Client client = clientRepo.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        Sort sort = Sort.by(Sort.Direction.ASC, "startDate");
        return nutritionPlanRepo.findByClientIdAndClientCoachId(clientId, coachId, sort);
    }

    public List<NutritionPlan> getNutritionPlans(UUID clientId) {
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        Sort sort = Sort.by(Sort.Direction.ASC, "startDate");
        return nutritionPlanRepo.findByClientId(clientId, sort);
    }
}
