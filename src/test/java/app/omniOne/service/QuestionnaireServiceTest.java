package app.omniOne.service;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.dto.QuestionnaireAnswerRequest;
import app.omniOne.model.dto.QuestionnaireAnswerResponse;
import app.omniOne.model.dto.QuestionnaireQuestionPostRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.QuestionnaireAnswerRepo;
import app.omniOne.repository.QuestionnaireQuestionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class QuestionnaireServiceTest {

    @Mock private CoachRepo coachRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private QuestionnaireAnswerRepo answerRepo;
    @Mock private QuestionnaireQuestionRepo questionRepo;
    @InjectMocks private QuestionnaireService questionnaireService;

    @Captor ArgumentCaptor<List<QuestionnaireAnswer>> captor;

    private UUID coachId;
    private UUID clientId;
    private Coach coach;
    private Client client;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        coach = new Coach();
        coach.setId(coachId);
        client = new Client();
        client.setId(clientId);
    }

    @Test void getQuestionsForCoach_returnsQuestionsFromRepo() {
        List<QuestionnaireQuestion> questions = List.of(new QuestionnaireQuestion(), new QuestionnaireQuestion());
        when(questionRepo.findAllByCoachIdOrCoachIdIsNull(coachId)).thenReturn(questions);

        List<QuestionnaireQuestion> result = questionnaireService.getQuestionsForCoach(coachId);

        assertEquals(questions, result);
        verify(questionRepo).findAllByCoachIdOrCoachIdIsNull(coachId);
        verifyNoInteractions(coachRepo, clientRepo, answerRepo);
    }

    @Test void addQuestion_persistsQuestionWithCoachAndText() {
        QuestionnaireQuestionPostRequest request = new QuestionnaireQuestionPostRequest("How are you?");

        when(coachRepo.findByIdOrThrow(coachId)).thenReturn(coach);
        when(questionRepo.save(any(QuestionnaireQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuestionnaireQuestion result = questionnaireService.addQuestion(coachId, request);

        assertSame(coach, result.getCoach());
        assertEquals("How are you?", result.getText());
        verify(coachRepo).findByIdOrThrow(coachId);
        verify(questionRepo).save(result);
    }

    @Test void deleteQuestion_removesQuestionFromRepo() {
        long questionId = 10L;
        QuestionnaireQuestion question = new QuestionnaireQuestion();
        question.setId(questionId);
        when(questionRepo.findByIdAndCoachIdOrThrow(questionId, coachId)).thenReturn(question);

        questionnaireService.deleteQuestion(coachId, questionId);

        verify(questionRepo).findByIdAndCoachIdOrThrow(questionId, coachId);
        verify(questionRepo).delete(question);
    }

    @Test void getQuestionsForClient_usesClientsCoach() {
        client.setCoach(coach);
        List<QuestionnaireQuestion> questions = List.of(new QuestionnaireQuestion());

        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        when(questionRepo.findAllByCoachIdOrCoachIdIsNull(coachId)).thenReturn(questions);

        List<QuestionnaireQuestion> result = questionnaireService.getQuestionsForClient(clientId);

        assertEquals(questions, result);
        verify(clientRepo).findByIdOrThrow(clientId);
        verify(questionRepo).findAllByCoachIdOrCoachIdIsNull(coachId);
    }

    @Test void putAnswers_updatesExistingAndCreatesNewAnswers() {
        client.setCoach(coach);

        QuestionnaireQuestion q1 = new QuestionnaireQuestion();
        q1.setId(1L);
        q1.setCoach(coach);
        QuestionnaireQuestion q2 = new QuestionnaireQuestion();
        q2.setId(2L);
        q2.setCoach(coach);

        QuestionnaireAnswer existingAnswer = new QuestionnaireAnswer();
        existingAnswer.setQuestion(q1);
        existingAnswer.setClient(client);

        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        when(questionRepo.findByIdAOrThrow(1L)).thenReturn(q1);
        when(questionRepo.findByIdAOrThrow(2L)).thenReturn(q2);
        when(answerRepo.findByClientIdAndQuestionId(clientId, 1L)).thenReturn(Optional.of(existingAnswer));
        when(answerRepo.findByClientIdAndQuestionId(clientId, 2L)).thenReturn(Optional.empty());

        List<QuestionnaireAnswerRequest> requests = List.of(
                new QuestionnaireAnswerRequest(1L, "existing"),
                new QuestionnaireAnswerRequest(2L, "new")
        );

        questionnaireService.putAnswers(clientId, requests);

        assertEquals("existing", existingAnswer.getAnswer());

        verify(answerRepo).saveAll(captor.capture());
        List<QuestionnaireAnswer> savedAnswers = captor.getValue();
        assertEquals(2, savedAnswers.size());
        QuestionnaireAnswer newAnswer = savedAnswers.stream()
                .filter(a -> a.getQuestion() == q2).findFirst().orElseThrow();
        assertSame(client, newAnswer.getClient());
        assertEquals("new", newAnswer.getAnswer());

        verify(clientRepo).findByIdOrThrow(clientId);
        verify(questionRepo).findByIdAOrThrow(1L);
        verify(questionRepo).findByIdAOrThrow(2L);
    }

    @Test void putAnswers_throwsWhenQuestionBelongsToDifferentCoach() {
        UUID otherCoachId = UUID.randomUUID();

        Coach coach = new Coach();
        coach.setId(coachId);
        Coach otherCoach = new Coach();
        otherCoach.setId(otherCoachId);
        Client client = new Client();
        client.setId(clientId);
        client.setCoach(coach);

        QuestionnaireQuestion question = new QuestionnaireQuestion();
        question.setId(5L);
        question.setCoach(otherCoach);

        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        when(questionRepo.findByIdAOrThrow(5L)).thenReturn(question);

        List<QuestionnaireAnswerRequest> requests =
                List.of(new QuestionnaireAnswerRequest(5L, "answer"));

        assertThrows(NoSuchResourceException.class,
                () -> questionnaireService.putAnswers(clientId, requests),
                "Expected rejection when question belongs to another coach");

        verify(answerRepo, never()).saveAll(any());
    }

    @Test void getAnswers_mapsEntitiesToResponses() {
        QuestionnaireQuestion q1 = new QuestionnaireQuestion();
        q1.setId(1L);
        q1.setText("Q1");
        QuestionnaireAnswer a1 = new QuestionnaireAnswer();
        a1.setQuestion(q1);
        a1.setAnswer("A1");

        QuestionnaireQuestion q2 = new QuestionnaireQuestion();
        q2.setId(2L);
        q2.setText("Q2");
        QuestionnaireAnswer a2 = new QuestionnaireAnswer();
        a2.setQuestion(q2);
        a2.setAnswer("A2");

        when(answerRepo.findAllByClientId(clientId)).thenReturn(List.of(a1, a2));

        List<QuestionnaireAnswerResponse> result = questionnaireService.getAnswers(clientId);

        List<QuestionnaireAnswerResponse> expected = List.of(
                new QuestionnaireAnswerResponse(1L, "Q1", "A1"),
                new QuestionnaireAnswerResponse(2L, "Q2", "A2")
        );
        assertEquals(expected, result);

        verify(answerRepo).findAllByClientId(clientId);
    }
}
