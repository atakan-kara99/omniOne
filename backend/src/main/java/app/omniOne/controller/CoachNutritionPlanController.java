package app.omniOne.controller;

import app.omniOne.model.dto.NutritionPlanPostDto;
import app.omniOne.model.dto.NutritionPlanResponseDto;
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
@RequestMapping("/coach/{coachId}/clients/{clientId}")
@PreAuthorize("@authService.isOwner(#coachId)")
public class CoachNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanMapper nutritionPlanMapper;

    @PostMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto addNutritionPlan(
            @PathVariable UUID coachId, @PathVariable UUID clientId,
            @RequestBody @Valid NutritionPlanPostDto dto) {
        return nutritionPlanMapper.map(nutritionPlanService.addNutritionPlan(coachId, clientId, dto));
    }

    @GetMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto getNutritionPlan(@PathVariable UUID coachId, @PathVariable UUID clientId) {
        return nutritionPlanMapper.map((nutritionPlanService.getActiveNutritionPlan(coachId, clientId)));
    }

    @GetMapping("/nutrition-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutritionPlanResponseDto> getNutritionPlans(@PathVariable UUID coachId, @PathVariable UUID clientId) {
        return nutritionPlanService.getNutritionPlans(coachId, clientId)
                        .stream().map(nutritionPlanMapper::map).toList();
    }

}
