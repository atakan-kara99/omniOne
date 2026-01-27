package app.omniOne.controller.client;

import app.omniOne.model.dto.CoachResponse;
import app.omniOne.service.ClientService;
import app.omniOne.service.CoachingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@Tag(name = "Client - Coach")
@RequestMapping("/client/coach")
public class ClientCoachController {

    private final ClientService clientService;
    private final CoachingService coachingService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CoachResponse getCoach() {
        return clientService.getCoach(getMyId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endCoaching() {
        coachingService.endCoaching(getMyId());
    }

}
