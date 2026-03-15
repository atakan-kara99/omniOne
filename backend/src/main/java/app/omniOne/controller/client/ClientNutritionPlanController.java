package app.omniOne.controller.client;

import app.omniOne.model.dto.NutritionPlanResponse;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Client - Nutrition Plan")
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientNutritionPlanController {

    private final NutritionPlanMapper nutritionPlanMapper;
    private final NutritionPlanService nutritionPlanService;

    @GetMapping("/nutri-plans/active")
    @ResponseStatus(HttpStatus.OK)
    public NutritionPlanResponse getNutriPlan() {
        return nutritionPlanMapper.map(nutritionPlanService.getActiveNutriPlan(getMyId()));
    }

    @GetMapping("/nutri-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutritionPlanResponse> getNutriPlans() {
        return nutritionPlanService.getNutriPlans(getMyId())
                        .stream().map(nutritionPlanMapper::map).collect(Collectors.toList());
    }

}
