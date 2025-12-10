package app.omniOne.controller.coach;

import app.omniOne.model.dto.CoachPatchRequest;
import app.omniOne.model.dto.CoachResponse;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coach")
public class CoachCoachController {

    private final CoachMapper coachMapper;
    private final CoachService coachService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CoachResponse getCoach() {
        return coachMapper.map(coachService.getCoach(getMyId()));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public CoachResponse patchCoach(@RequestBody @Valid CoachPatchRequest dto){
        return coachMapper.map(coachService.patchCoach(getMyId(), dto));
    }

}
