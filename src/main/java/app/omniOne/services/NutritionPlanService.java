package app.omniOne.services;

import app.omniOne.exceptions.NoSuchResourceException;
import app.omniOne.models.dtos.NutritionPlanPostDto;
import app.omniOne.models.entities.Client;
import app.omniOne.models.entities.NutritionPlan;
import app.omniOne.models.mappers.NutritionPlanMapper;
import app.omniOne.repositories.ClientRepository;
import app.omniOne.repositories.NutritionPlanRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final ClientRepository clientRepository;
    private final NutritionPlanMapper nutritionPlanMapper;

    public NutritionPlanService(
            NutritionPlanRepository nutritionPlanRepository,
            ClientRepository clientRepository,
            NutritionPlanMapper nutritionPlanMapper) {
        this.nutritionPlanRepository = nutritionPlanRepository;
        this.clientRepository = clientRepository;
        this.nutritionPlanMapper = nutritionPlanMapper;
    }

    @Transactional
    public NutritionPlan addNutritionPlan(Long coachId, Long clientId, NutritionPlanPostDto postDto) {
        Client client = clientRepository.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        nutritionPlanRepository.findByClientIdAndClientCoachIdAndEndDateIsNull(clientId, coachId)
                .ifPresent(activeNP -> activeNP.setEndDate(LocalDate.now()));
        NutritionPlan newPlan = new NutritionPlan(
                postDto.carbohydrates(),
                postDto.proteins(),
                postDto.fats(),
                client
        );
        return nutritionPlanRepository.save(newPlan);
    }

    public NutritionPlan getActiveNutritionPlan(Long coachId, Long clientId) {
        return nutritionPlanRepository.findByClientIdAndClientCoachIdAndEndDateIsNull(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d has no NutritionPlan".formatted(clientId)));
    }

    public List<NutritionPlan> getNutritionPlans(Long coachId, Long clientId) {
        Client client = clientRepository.findByIdAndCoachId(clientId, coachId)
                .orElseThrow(() -> new NoSuchResourceException("Client %d not found".formatted(clientId)));
        Sort sort = Sort.by(Sort.Direction.ASC, "startDate");
        return nutritionPlanRepository.findByClientIdAndClientCoachId(clientId, coachId, sort);
    }

}
