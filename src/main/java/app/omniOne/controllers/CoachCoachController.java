package app.omniOne.controllers;

import app.omniOne.models.dtos.CoachPatchDto;
import app.omniOne.models.dtos.CoachPostDto;
import app.omniOne.models.dtos.CoachResponseDto;
import app.omniOne.models.mappers.CoachMapper;
import app.omniOne.services.CoachService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coach")
public class CoachCoachController {

    private final CoachService coachService;
    private final CoachMapper coachMapper;

    public CoachCoachController(CoachService coachService, CoachMapper coachMapper) {
        this.coachService = coachService;
        this.coachMapper = coachMapper;
    }

    @PostMapping
    public ResponseEntity<CoachResponseDto> registerCoach(@RequestBody @Valid CoachPostDto postDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coachMapper.convert(coachService.registerCoach(postDto)));
    }

    @GetMapping("/{coachId}")
    public ResponseEntity<CoachResponseDto> getCoach(@PathVariable Long coachId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(coachMapper.convert(coachService.getCoach(coachId)));
    }

    @PatchMapping("/{coachId}")
    public ResponseEntity<CoachResponseDto> patchCoach(
            @PathVariable Long coachId, @RequestBody @Valid CoachPatchDto patchDto){
        return ResponseEntity.status(HttpStatus.OK)
                .body(coachMapper.convert(coachService.patchCoach(coachId, patchDto)));
    }

    @DeleteMapping("/{coach_id}")
    public ResponseEntity<Void> deleteCoach(@PathVariable Long coachId) {
        coachService.deleteCoach(coachId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
