package app.omniOne.controller.coach;

import app.omniOne.model.dto.SupplementPlanRequest;
import app.omniOne.model.dto.SupplementPlanResponse;
import app.omniOne.model.mapper.SupplementPlanMapper;
import app.omniOne.service.SupplementPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Coach - Supplement Plan")
@RequiredArgsConstructor
@RequestMapping("/coach/clients/{clientId}")
@PreAuthorize("@authService.isCoachedByMe(#clientId)")
public class CoachSupplementPlanController {

    private final SupplementPlanMapper supplementPlanMapper;
    private final SupplementPlanService supplementPlanService;

    @PostMapping("/supplement-plans")
    @ResponseStatus(HttpStatus.CREATED)
    public SupplementPlanResponse addSupplementPlan(
            @PathVariable UUID clientId, @RequestBody @Valid SupplementPlanRequest request) {
        return supplementPlanMapper.map(supplementPlanService.addSupplementPlan(clientId, request));
    }

    @PutMapping("/supplement-plans/{planId}")
    @ResponseStatus(HttpStatus.OK)
    public SupplementPlanResponse correctSupplementPlan(
            @PathVariable UUID clientId, @PathVariable Long planId,
            @RequestBody @Valid SupplementPlanRequest request) {
        return supplementPlanMapper.map(supplementPlanService.correctSupplementPlan(clientId, planId, request));
    }

    @GetMapping("/supplement-plans/active")
    @ResponseStatus(HttpStatus.OK)
    public SupplementPlanResponse getActiveSupplementPlan(@PathVariable UUID clientId) {
        return supplementPlanMapper.map(supplementPlanService.getActiveSupplementPlan(clientId));
    }

    @GetMapping("/supplement-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<SupplementPlanResponse> getSupplementPlans(@PathVariable UUID clientId) {
        return supplementPlanService.getSupplementPlans(clientId)
                .stream().map(supplementPlanMapper::map).toList();
    }

    @DeleteMapping("/supplement-plans/{planId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplementPlan(@PathVariable UUID clientId, @PathVariable Long planId) {
        supplementPlanService.deleteSupplementPlan(clientId, planId);
    }

}
