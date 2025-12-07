package app.omniOne.controller.coach;

import app.omniOne.model.dto.NutritionPlanPostDto;
import app.omniOne.model.dto.NutritionPlanResponseDto;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static app.omniOne.auth.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coach/clients/{clientId}")
public class CoachNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanMapper nutritionPlanMapper;

    @PostMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto addNutritionPlan(
            @PathVariable UUID clientId, @RequestBody @Valid NutritionPlanPostDto dto) {
        return nutritionPlanMapper.map(nutritionPlanService.addNutritionPlan(getMyId(), clientId, dto));
    }

    @GetMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto getNutritionPlan(@PathVariable UUID clientId) {
        return nutritionPlanMapper.map((nutritionPlanService.getActiveNutritionPlan(getMyId(), clientId)));
    }

    @GetMapping("/nutrition-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutritionPlanResponseDto> getNutritionPlans(@PathVariable UUID clientId) {
        return nutritionPlanService.getNutritionPlans(getMyId(), clientId)
                        .stream().map(nutritionPlanMapper::map).toList();
    }

}
