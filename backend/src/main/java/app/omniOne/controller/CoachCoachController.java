package app.omniOne.controller;

import app.omniOne.model.dto.CoachPatchDto;
import app.omniOne.model.dto.CoachResponseDto;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coach/{coachId}")
@PreAuthorize("@authService.isOwner(#coachId)")
public class CoachCoachController {

    private final CoachMapper coachMapper;
    private final CoachService coachService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CoachResponseDto getCoach(@PathVariable UUID coachId) {
        return coachMapper.map(coachService.getCoach(coachId));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public CoachResponseDto patchCoach(@PathVariable UUID coachId, @RequestBody @Valid CoachPatchDto dto){
        return coachMapper.map(coachService.patchCoach(coachId, dto));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoach(@PathVariable UUID coachId) {
        coachService.deleteCoach(coachId);
        //return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
