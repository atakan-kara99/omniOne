package app.omniOne.controller.client;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.jwt.JwtFilter;
import app.omniOne.model.dto.QuestionnaireAnswerRequest;
import app.omniOne.model.dto.QuestionnaireAnswerResponse;
import app.omniOne.model.dto.QuestionnaireQuestionResponse;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import app.omniOne.model.mapper.QuestionnaireMapper;
import app.omniOne.service.QuestionnaireService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static app.omniOne.TestFixtures.question;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ClientQuestionnaireController.class)
class ClientQuestionnaireControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private QuestionnaireMapper questionnaireMapper;
    @MockitoBean private QuestionnaireService questionnaireService;

    private UUID clientId;

    @BeforeEach void setUp() {
        clientId = UUID.randomUUID();
        mockAuthenticatedUser(clientId);
    }

    @Test void getQuestions_returnsMappedList() throws Exception {
        QuestionnaireQuestion question = question(1L, null, "How do you feel?");
        QuestionnaireQuestionResponse response = new QuestionnaireQuestionResponse(1L, "How do you feel?");

        when(questionnaireService.getQuestionsForClient(clientId)).thenReturn(List.of(question));
        when(questionnaireMapper.map(question)).thenReturn(response);

        mockMvc.perform(get("/client/questionnaire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("How do you feel?"));

        verify(questionnaireService).getQuestionsForClient(clientId);
        verify(questionnaireMapper).map(question);
    }

    @Test void putAnswers_savesAnswers() throws Exception {
        List<QuestionnaireAnswerRequest> requests = List.of(
                new QuestionnaireAnswerRequest(1L, "Yes"),
                new QuestionnaireAnswerRequest(2L, "No"));

        mockMvc.perform(put("/client/questionnaire/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk());

        verify(questionnaireService).putAnswers(clientId, requests);
    }

    @Test void putAnswers_returnsBadRequestOnValidationError() throws Exception {
        List<QuestionnaireAnswerRequest> invalid =
                List.of(new QuestionnaireAnswerRequest(null, "answer"));

        mockMvc.perform(put("/client/questionnaire/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.questionId").value("must not be null"));

        verifyNoInteractions(questionnaireService);
        verifyNoInteractions(questionnaireMapper);
    }

    @Test void getAnswers_returnsResponses() throws Exception {
        QuestionnaireAnswerResponse answer =
                new QuestionnaireAnswerResponse(3L, "Q?", "A!");
        when(questionnaireService.getAnswers(clientId)).thenReturn(List.of(answer));

        mockMvc.perform(get("/client/questionnaire/answers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionId").value(3L))
                .andExpect(jsonPath("$[0].answerText").value("A!"));

        verify(questionnaireService).getAnswers(clientId);
    }
}
