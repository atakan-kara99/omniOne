package app.omniOne.controller.client;

import app.omniOne.model.dto.NutriPlanResponse;
import app.omniOne.model.mapper.NutriPlanMapper;
import app.omniOne.service.NutriPlanService;
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
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientNutriPlanController {

    private final NutriPlanMapper nutriPlanMapper;
    private final NutriPlanService nutriPlanService;

    @GetMapping("/nutri-plans/active")
    @ResponseStatus(HttpStatus.OK)
    public NutriPlanResponse getNutriPlan() {
        return nutriPlanMapper.map(nutriPlanService.getActiveNutriPlan(getMyId()));
    }

    @GetMapping("/nutri-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<NutriPlanResponse> getNutriPlans() {
        return nutriPlanService.getNutriPlans(getMyId())
                        .stream().map(nutriPlanMapper::map).collect(Collectors.toList());
    }

}
