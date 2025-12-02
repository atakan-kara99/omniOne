package app.omniOne.controllers;

import app.omniOne.models.dtos.CoachDto;
import app.omniOne.models.entities.Coach;
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
    public ResponseEntity<CoachDto> registerCoach(@RequestBody @Valid Coach coach) {
        Coach newCoach = coachService.registerCoach(coach);
        CoachDto newCoachDto = coachMapper.toCoachDto(newCoach);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCoachDto);
    }

    @GetMapping("/{coachId}")
    public ResponseEntity<CoachDto> getCoach(@PathVariable Long coachId) {
        Coach coach = coachService.getCoach(coachId);
        CoachDto coachDto = coachMapper.toCoachDto(coach);
        return ResponseEntity.status(HttpStatus.OK).body(coachDto);
    }

    @PutMapping("/{coachId}")
    public ResponseEntity<CoachDto> updateCoach(@PathVariable Long coachId, @RequestBody @Valid Coach coach){
        CoachDto coachDto = coachMapper.toCoachDto(coachService.updateCoach(coachId, coach));
        return ResponseEntity.status(HttpStatus.OK).body(coachDto);
    }

    @DeleteMapping("/{coach_id}")
    public ResponseEntity<Void> deleteCoach(@PathVariable Long coachId) {
        coachService.deleteCoach(coachId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
