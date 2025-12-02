package app.omniOne.controllers;

import app.omniOne.models.dtos.NutritionPlanPostDto;
import app.omniOne.models.dtos.NutritionPlanResponseDto;
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

    public CoachNutritionPlanController(
            NutritionPlanService nutritionPlanService, NutritionPlanMapper nutritionPlanMapper) {
        this.nutritionPlanService = nutritionPlanService;
        this.nutritionPlanMapper = nutritionPlanMapper;
    }

    @PostMapping("/nutrition-plan")
    public ResponseEntity<NutritionPlanResponseDto> addNutritionPlan(
            @PathVariable Long coachId, @PathVariable Long clientId,
            @RequestBody @Valid NutritionPlanPostDto postDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(nutritionPlanMapper.convert(nutritionPlanService.addNutritionPlan(coachId, clientId, postDto)));
    }

    @GetMapping("/nutrition-plan")
    public ResponseEntity<NutritionPlanResponseDto> getNutritionPlan(
            @PathVariable Long coachId, @PathVariable Long clientId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(nutritionPlanMapper.convert((nutritionPlanService.getActiveNutritionPlan(coachId, clientId))));
    }

    @GetMapping("/nutrition-plans")
    public ResponseEntity<List<NutritionPlanResponseDto>> getNutritionPlans(
            @PathVariable Long coachId, @PathVariable Long clientId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(nutritionPlanService.getNutritionPlans(coachId, clientId)
                        .stream().map(nutritionPlanMapper::convert).toList());
    }

}
