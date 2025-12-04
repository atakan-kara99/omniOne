package app.omniOne.controller;

import app.omniOne.model.dto.CoachPatchDto;
import app.omniOne.model.dto.CoachPostDto;
import app.omniOne.model.dto.CoachResponseDto;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coach")
public class CoachCoachController {

    private final CoachService coachService;
    private final CoachMapper coachMapper;

    @PostMapping
    public ResponseEntity<CoachResponseDto> registerCoach(@RequestBody @Valid CoachPostDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coachMapper.map(coachService.registerCoach(dto)));
    }

    @GetMapping("/{coachId}")
    @ResponseStatus(HttpStatus.OK)
    public CoachResponseDto getCoach(@PathVariable Long coachId) {
        return coachMapper.map(coachService.getCoach(coachId));
    }

    @PatchMapping("/{coachId}")
    @ResponseStatus(HttpStatus.OK)
    public CoachResponseDto patchCoach(@PathVariable Long coachId, @RequestBody @Valid CoachPatchDto dto){
        return coachMapper.map(coachService.patchCoach(coachId, dto));
    }

    @DeleteMapping("/{coach_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoach(@PathVariable Long coachId) {
        coachService.deleteCoach(coachId);
        //return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
