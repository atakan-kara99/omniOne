package app.omniOne.controllers;

import app.omniOne.models.dtos.NutritionPlanDto;
import app.omniOne.models.entities.NutritionPlan;
import app.omniOne.models.mappers.NutritionPlanMapper;
import app.omniOne.services.NutritionPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coach/{coachId}/clients/{clientId}")
public class CoachNutritionPlanController {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanMapper nutritionPlanMapper;

    public CoachNutritionPlanController(NutritionPlanService nutritionPlanService, NutritionPlanMapper nutritionPlanMapper) {
        this.nutritionPlanService = nutritionPlanService;
        this.nutritionPlanMapper = nutritionPlanMapper;
    }

    @PostMapping("/nutrition-plan")
    public ResponseEntity<NutritionPlanDto> addNutritionPlan(@PathVariable Long coachId, @PathVariable Long clientId, @RequestBody @Valid NutritionPlan np) {
        return ResponseEntity.status(HttpStatus.OK).body(nutritionPlanMapper.toNutritionPlanDto(nutritionPlanService.addNutritionPlan(coachId, clientId, np)));
    }

    @GetMapping("/nutrition-plan")
    public ResponseEntity<NutritionPlanDto> getNutritionPlan(@PathVariable Long coachId, @PathVariable Long clientId) {
        NutritionPlanDto npd = nutritionPlanMapper.toNutritionPlanDto(nutritionPlanService.getNutritionPlan(coachId, clientId));
        return ResponseEntity.status(HttpStatus.OK).body(npd);
    }

    @GetMapping("/nutrition-plans")
    public ResponseEntity<List<NutritionPlanDto>> getNutritionPlans(@PathVariable Long coachId, @PathVariable Long clientId) {
        List<NutritionPlan> nps = nutritionPlanService.getNutritionPlans(coachId, clientId);
        List<NutritionPlanDto> npds = nps.stream().map(nutritionPlanMapper::toNutritionPlanDto).toList();
        return ResponseEntity.status(HttpStatus.OK).body(npds);
    }

}
