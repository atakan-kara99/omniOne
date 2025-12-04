package app.omniOne.controller;

import app.omniOne.model.dto.NutritionPlanResponseDto;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/{clientId}")
public class ClientNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanMapper nutritionPlanMapper;

    @GetMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto getNutritionPlan(@PathVariable Long clientId) {
        return nutritionPlanMapper.map(nutritionPlanService.getActiveNutritionPlan(clientId));
    }

    @GetMapping("/nutrition-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutritionPlanResponseDto> getNutritionPlans(@PathVariable Long clientId) {
        return nutritionPlanService.getNutritionPlans(clientId)
                        .stream().map(nutritionPlanMapper::map).collect(Collectors.toList());
    }

}
