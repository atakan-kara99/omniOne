package app.omniOne.controller.client;

import app.omniOne.model.dto.NutritionPlanResponseDto;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static app.omniOne.auth.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanMapper nutritionPlanMapper;

    @GetMapping("/nutrition-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponseDto getNutritionPlan() {
        return nutritionPlanMapper.map(nutritionPlanService.getActiveNutritionPlan(getMyId()));
    }

    @GetMapping("/nutrition-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutritionPlanResponseDto> getNutritionPlans() {
        return nutritionPlanService.getNutritionPlans(getMyId())
                        .stream().map(nutritionPlanMapper::map).collect(Collectors.toList());
    }

}
