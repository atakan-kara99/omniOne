package app.omniOne.controller.coach;

import app.omniOne.model.dto.NutriPlanPostRequest;
import app.omniOne.model.dto.NutriPlanResponseDto;
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

    @PostMapping("/nutri-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutriPlanResponseDto addNutriPlan(
            @PathVariable UUID clientId, @RequestBody @Valid NutriPlanPostRequest dto) {
        return nutriPlanMapper.map(nutriPlanService.addNutriPlan(clientId, dto));
    }

    @GetMapping("/nutri-plan")
    @ResponseStatus(HttpStatus.OK)
    public NutriPlanResponseDto getNutriPlan(@PathVariable UUID clientId) {
        return nutriPlanMapper.map((nutriPlanService.getActiveNutriPlan(clientId)));
    }

    @GetMapping("/nutri-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutriPlanResponseDto> getNutriPlans(@PathVariable UUID clientId) {
        return nutriPlanService.getNutriPlans(clientId)
                        .stream().map(nutriPlanMapper::map).toList();
    }

}
