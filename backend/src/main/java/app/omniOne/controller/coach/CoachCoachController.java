package app.omniOne.controller.coach;

import app.omniOne.model.dto.CoachResponse;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.service.CoachService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Coach")
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
//
//    @PatchMapping
//    @ResponseStatus(HttpStatus.OK)
//    public CoachResponse patchCoach(@RequestBody @Valid CoachPatchRequest dto){
//        return coachMapper.map(coachService.patchCoach(getMyId(), dto));
//    }

}
