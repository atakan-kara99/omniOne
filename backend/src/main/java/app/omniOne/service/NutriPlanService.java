package app.omniOne.service;

import app.omniOne.model.dto.NutriPlanPostRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.NutriPlan;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.NutriPlanRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
        return nutriPlanRepo.save(newPlan);
    }

    public NutriPlan getActiveNutriPlan(UUID clientId) {
        return nutriPlanRepo.findByClientIdAndEndDateIsNullOrThrow(clientId);
    }

    public List<NutriPlan> getNutriPlans(UUID clientId) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        Sort sort = Sort.by(Sort.Direction.ASC, "startDate");
        return nutriPlanRepo.findByClientId(clientId, sort);
    }
}
