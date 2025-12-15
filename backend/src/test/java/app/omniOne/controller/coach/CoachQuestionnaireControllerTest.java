package app.omniOne.controller.coach;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.AuthService;
import app.omniOne.authentication.jwt.JwtFilter;
import app.omniOne.model.dto.QuestionnaireAnswerResponse;
import app.omniOne.model.dto.QuestionnaireQuestionPostRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CoachQuestionnaireController.class)
class CoachQuestionnaireControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private AuthService authService;
    @MockitoBean private QuestionnaireMapper questionnaireMapper;
    @MockitoBean private QuestionnaireService questionnaireService;

    private UUID coachId;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        mockAuthenticatedUser(coachId);
        when(authService.isCoachedByMe(any())).thenReturn(true);
    }

    @Test void getQuestions_returnsMappedList() throws Exception {
        QuestionnaireQuestion question = question(1L, null, "How are you?");
        QuestionnaireQuestionResponse response = new QuestionnaireQuestionResponse(1L, "How are you?");

        when(questionnaireService.getQuestionsForCoach(coachId)).thenReturn(List.of(question));
        when(questionnaireMapper.map(question)).thenReturn(response);

        mockMvc.perform(get("/coach/questionnaire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("How are you?"));

        verify(questionnaireService).getQuestionsForCoach(coachId);
        verify(questionnaireMapper).map(question);
    }

    @Test void addQuestion_createsAndReturnsResponse() throws Exception {
        QuestionnaireQuestionPostRequest request = new QuestionnaireQuestionPostRequest("New question?");
        QuestionnaireQuestion question = question(2L, null, "New question?");
        QuestionnaireQuestionResponse response = new QuestionnaireQuestionResponse(2L, "New question?");

        when(questionnaireService.addQuestion(eq(coachId), any(QuestionnaireQuestionPostRequest.class)))
                .thenReturn(question);
        when(questionnaireMapper.map(question)).thenReturn(response);

        mockMvc.perform(post("/coach/questionnaire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.text").value("New question?"));

        verify(questionnaireService).addQuestion(eq(coachId), any(QuestionnaireQuestionPostRequest.class));
        verify(questionnaireMapper).map(question);
    }

    @Test void deleteQuestion_removesQuestion() throws Exception {
        long questionId = 3L;

        mockMvc.perform(delete("/coach/questionnaire/{questionId}", questionId))
                .andExpect(status().isNoContent());

        verify(questionnaireService).deleteQuestion(coachId, questionId);
    }

    @Test void getAnswers_returnsAnswerList() throws Exception {
        UUID clientId = UUID.randomUUID();
        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        QuestionnaireAnswerResponse answer =
                new QuestionnaireAnswerResponse(5L, "Q?", "A!");
        when(questionnaireService.getAnswers(clientId)).thenReturn(List.of(answer));

        mockMvc.perform(get("/coach/questionnaire/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionId").value(5L))
                .andExpect(jsonPath("$[0].answerText").value("A!"));

        verify(questionnaireService).getAnswers(clientId);
    }

    @Test void addQuestion_returnsBadRequestOnValidationFailure() throws Exception {
        QuestionnaireQuestionPostRequest invalid = new QuestionnaireQuestionPostRequest("");

        mockMvc.perform(post("/coach/questionnaire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.text").value("must not be blank"));

        verifyNoInteractions(questionnaireService);
        verifyNoInteractions(questionnaireMapper);
    }
}
