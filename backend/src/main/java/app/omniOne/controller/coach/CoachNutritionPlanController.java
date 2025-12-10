package app.omniOne.controller.coach;

import app.omniOne.model.dto.NutritionPlanRequest;
import app.omniOne.model.dto.NutritionPlanResponse;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coach/clients/{clientId}")
@PreAuthorize("@authService.isCoachedByMe(#clientId)")
public class CoachNutritionPlanController {

    private final NutritionPlanMapper nutritionPlanMapper;
    private final NutritionPlanService nutritionPlanService;

    @PostMapping("/nutri-plans")
    @ResponseStatus(HttpStatus.CREATED)
    public NutritionPlanResponse addNutriPlan(
            @PathVariable UUID clientId, @RequestBody @Valid NutritionPlanRequest request) {
        return nutritionPlanMapper.map(nutritionPlanService.addNutriPlan(clientId, request));
    }

    @PutMapping("/nutri-plans/{nutriPlanId}")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponse correctNutriPlan(
            @PathVariable UUID clientId, @PathVariable Long nutriPlanId,
            @RequestBody @Valid NutritionPlanRequest request) {
        return nutritionPlanMapper.map(nutritionPlanService.correctNutriPlan(clientId, nutriPlanId, request));
    }

    @GetMapping("/nutri-plans/active")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponse getNutriPlan(@PathVariable UUID clientId) {
        return nutritionPlanMapper.map((nutritionPlanService.getActiveNutriPlan(clientId)));
    }

    @GetMapping("/nutri-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutritionPlanResponse> getNutriPlans(@PathVariable UUID clientId) {
        return nutritionPlanService.getNutriPlans(clientId)
                        .stream().map(nutritionPlanMapper::map).toList();
    }

}
