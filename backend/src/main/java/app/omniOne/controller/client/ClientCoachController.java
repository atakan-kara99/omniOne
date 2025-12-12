package app.omniOne.controller.client;

import app.omniOne.service.CoachingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Client - Coach")
@RequestMapping("/client/coach")
public class ClientCoachController {

    private CoachingService coachingService;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endCoaching() {
        coachingService.endCoaching(getMyId());
    }

}
