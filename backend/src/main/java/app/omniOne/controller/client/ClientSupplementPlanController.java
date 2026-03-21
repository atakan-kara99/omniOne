package app.omniOne.controller.client;

import app.omniOne.model.dto.SupplementPlanResponse;
import app.omniOne.model.mapper.SupplementPlanMapper;
import app.omniOne.service.SupplementPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Client - Supplement Plan")
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientSupplementPlanController {

    private final SupplementPlanMapper supplementPlanMapper;
    private final SupplementPlanService supplementPlanService;

    @GetMapping("/supplement-plans/active")
    @ResponseStatus(HttpStatus.OK)
    public SupplementPlanResponse getActiveSupplementPlan() {
        return supplementPlanMapper.map(supplementPlanService.getActiveSupplementPlan(getMyId()));
    }

    @GetMapping("/supplement-plans")
    @ResponseStatus(HttpStatus.OK)
    public List<SupplementPlanResponse> getSupplementPlans() {
        return supplementPlanService.getSupplementPlans(getMyId())
                .stream().map(supplementPlanMapper::map).toList();
    }

}
