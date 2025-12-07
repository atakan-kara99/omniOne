package app.omniOne.controller.coach;

import app.omniOne.model.dto.CoachPatchDto;
import app.omniOne.model.dto.CoachResponseDto;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static app.omniOne.auth.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coach")
public class CoachCoachController {

    private final CoachMapper coachMapper;
    private final CoachService coachService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CoachResponseDto getCoach() {
        return coachMapper.map(coachService.getCoach(getMyId()));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public CoachResponseDto patchCoach(@RequestBody @Valid CoachPatchDto dto){
        return coachMapper.map(coachService.patchCoach(getMyId(), dto));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoach() {
        coachService.deleteCoach(getMyId());
    }

}
