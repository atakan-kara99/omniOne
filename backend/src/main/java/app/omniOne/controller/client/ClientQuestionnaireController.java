package app.omniOne.controller.client;

import app.omniOne.model.dto.QuestionnaireAnswerRequest;
import app.omniOne.model.dto.QuestionnaireAnswerResponse;
import app.omniOne.model.dto.QuestionnaireQuestionResponse;
import app.omniOne.model.mapper.QuestionnaireMapper;
import app.omniOne.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Client - Questionnaire")
@RequiredArgsConstructor
@RequestMapping("/client/questionnaire")
public class ClientQuestionnaireController {

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionnaireService questionnaireService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<QuestionnaireQuestionResponse> getQuestions() {
        return questionnaireService.getQuestionsForClient(getMyId()).stream().map(questionnaireMapper::map).toList();
    }

    @PutMapping("/answers")
    @ResponseStatus(HttpStatus.OK)
    public void putAnswers(@RequestBody @Valid List<QuestionnaireAnswerRequest> requests) {
        questionnaireService.putAnswers(getMyId(), requests);
    }

    @GetMapping("/answers")
    @ResponseStatus(HttpStatus.OK)
    public List<QuestionnaireAnswerResponse> getAnswers() {
        return questionnaireService.getAnswers(getMyId());
    }

}
