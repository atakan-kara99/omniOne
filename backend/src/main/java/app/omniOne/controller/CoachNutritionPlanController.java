package app.omniOne.controller;

import app.omniOne.model.dto.NutritionPlanPostDto;
import app.omniOne.model.dto.NutritionPlanResponseDto;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coach/{coachId}/clients/{clientId}")
public class CoachNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanMapper nutritionPlanMapper;

    @PostMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto addNutritionPlan(
            @PathVariable Long coachId, @PathVariable Long clientId,
            @RequestBody @Valid NutritionPlanPostDto dto) {
        return nutritionPlanMapper.map(nutritionPlanService.addNutritionPlan(coachId, clientId, dto));
    }

    @GetMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto getNutritionPlan(@PathVariable Long coachId, @PathVariable Long clientId) {
        return nutritionPlanMapper.map((nutritionPlanService.getActiveNutritionPlan(coachId, clientId)));
    }

    @GetMapping("/nutrition-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutritionPlanResponseDto> getNutritionPlans(@PathVariable Long coachId, @PathVariable Long clientId) {
        return nutritionPlanService.getNutritionPlans(coachId, clientId)
                        .stream().map(nutritionPlanMapper::map).toList();
    }

}
