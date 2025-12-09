package app.omniOne.controller.coach;

import app.omniOne.model.dto.NutriPlanRequest;
import app.omniOne.model.dto.NutriPlanResponse;
import app.omniOne.model.mapper.NutriPlanMapper;
import app.omniOne.service.NutriPlanService;
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
public class CoachNutriPlanController {

    private final NutriPlanMapper nutriPlanMapper;
    private final NutriPlanService nutriPlanService;

    @PostMapping("/nutri-plans")
    @ResponseStatus(HttpStatus.CREATED)
    public NutriPlanResponse addNutriPlan(
            @PathVariable UUID clientId, @RequestBody @Valid NutriPlanRequest request) {
        return nutriPlanMapper.map(nutriPlanService.addNutriPlan(clientId, request));
    }

    @PutMapping("/nutri-plans/{nutriPlanId}")
    @ResponseStatus(HttpStatus.OK)
    public NutriPlanResponse correctNutriPlan(
            @PathVariable UUID clientId, @PathVariable Long nutriPlanId,
            @RequestBody @Valid NutriPlanRequest request) {
        return nutriPlanMapper.map(nutriPlanService.correctNutriPlan(clientId, nutriPlanId, request));
    }

    @GetMapping("/nutri-plans/active")
    @ResponseStatus(HttpStatus.OK)
    public NutriPlanResponse getNutriPlan(@PathVariable UUID clientId) {
        return nutriPlanMapper.map((nutriPlanService.getActiveNutriPlan(clientId)));
    }

    @GetMapping("/nutri-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutriPlanResponse> getNutriPlans(@PathVariable UUID clientId) {
        return nutriPlanService.getNutriPlans(clientId)
                        .stream().map(nutriPlanMapper::map).toList();
    }

}
