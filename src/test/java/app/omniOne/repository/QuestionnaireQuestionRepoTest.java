package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class QuestionnaireQuestionRepoTest extends RepositoryTestBase {

    @Autowired private QuestionnaireQuestionRepo questionnaireQuestionRepo;

    private Coach coach;
    private QuestionnaireQuestion coachQuestion;
    private QuestionnaireQuestion standardQuestion;

    @BeforeEach void setUp() {
        coach = persistCoach(persistUser("coach@omni.one", UserRole.COACH));
        coachQuestion = persistQuestion("Coach question", coach);
        standardQuestion = persistQuestion("Standard question", null);
        flushAndClear();
    }

    @Test void findAllByCoachIdOrCoachIdIsNull_returnsCoachAndStandardQuestions() {
        List<QuestionnaireQuestion> questions =
                questionnaireQuestionRepo.findAllByCoachIdOrCoachIdIsNull(coach.getId());

        assertEquals(2, questions.size());
        assertTrue(questions.stream().anyMatch(q -> q.getId().equals(coachQuestion.getId())));
        assertTrue(questions.stream().anyMatch(q -> q.getId().equals(standardQuestion.getId())));
    }

    @Test void findByIdAndCoachIdOrThrow_returnsQuestionWhenPresent() {
        QuestionnaireQuestion result =
                questionnaireQuestionRepo.findByIdAndCoachIdOrThrow(coachQuestion.getId(), coach.getId());

        assertEquals(coachQuestion.getId(), result.getId());
    }

    @Test void findByIdAndCoachIdOrThrow_throwsWhenMissing() {
        NoSuchResourceException exception = assertThrows(
                NoSuchResourceException.class,
                () -> questionnaireQuestionRepo.findByIdAndCoachIdOrThrow(999L, coach.getId()));

        assertEquals("QuestionnaireQuestion not found", exception.getMessage());
    }

    @Test void findByIdAOrThrow_returnsQuestionWhenPresent() {
        QuestionnaireQuestion result = questionnaireQuestionRepo.findByIdAOrThrow(standardQuestion.getId());

        assertEquals(standardQuestion.getId(), result.getId());
    }

    @Test void findByIdAOrThrow_throwsWhenMissing() {
        NoSuchResourceException exception = assertThrows(
                NoSuchResourceException.class,
                () -> questionnaireQuestionRepo.findByIdAOrThrow(1000L));

        assertEquals("QuestionnaireQuestion not found", exception.getMessage());
    }
}
