package app.omniOne.controller.client;

import app.omniOne.service.CoachingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static app.omniOne.authentication.AuthService.getMyId;

@Controller
@RequestMapping("/client/coach")
public class ClientCoachController {

    private CoachingService coachingService;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endCoaching() {
        coachingService.endCoaching(getMyId());
    }

}
