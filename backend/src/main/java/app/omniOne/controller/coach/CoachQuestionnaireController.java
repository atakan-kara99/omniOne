package app.omniOne.controller.coach;

import app.omniOne.model.dto.QuestionnaireAnswerResponse;
import app.omniOne.model.dto.QuestionnaireQuestionPostRequest;
import app.omniOne.model.dto.QuestionnaireQuestionResponse;
import app.omniOne.model.mapper.QuestionnaireMapper;
import app.omniOne.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Coach - Questionnaire")
@RequiredArgsConstructor
@RequestMapping("/coach/questionnaire")
public class CoachQuestionnaireController {

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionnaireService questionnaireService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<QuestionnaireQuestionResponse> getQuestions() {
        return questionnaireService.getQuestionsForCoach(getMyId()).stream().map(questionnaireMapper::map).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionnaireQuestionResponse addQuestion(@RequestBody @Valid QuestionnaireQuestionPostRequest request) {
        return questionnaireMapper.map(questionnaireService.addQuestion(getMyId(), request));
    }

    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable Long questionId) {
        questionnaireService.deleteQuestion(getMyId(), questionId);
    }

    @GetMapping("/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authService.isCoachedByMe(#clientId)")
    public List<QuestionnaireAnswerResponse> getAnswers(@PathVariable UUID clientId) {
        return questionnaireService.getAnswers(clientId);
    }

}
