package app.omniOne.repository;

import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class QuestionnaireAnswerRepoTest extends RepositoryTestBase {

    @Autowired private QuestionnaireAnswerRepo questionnaireAnswerRepo;

    private Client client;
    private QuestionnaireQuestion question;
    private QuestionnaireAnswer savedAnswer;

    @BeforeEach void setUp() {
        Coach coach = persistCoach(persistUser("coach@omni.one", UserRole.COACH));
        client = persistClient(persistUser("client@omni.one", UserRole.CLIENT), coach);
        question = persistQuestion("Coach question", coach);
        savedAnswer = persistAnswer(client, question, "Yes");
        flushAndClear();
    }

    @Test void existsByClientIdAndQuestionId_matchesPersistedAnswer() {
        boolean exists = questionnaireAnswerRepo.existsByClientIdAndQuestionId(client.getId(), question.getId());
        boolean missing = questionnaireAnswerRepo.existsByClientIdAndQuestionId(client.getId(), 999L);

        assertTrue(exists);
        assertFalse(missing);
    }

    @Test void findByClientIdAndQuestionId_returnsAnswerWhenPresent() {
        Optional<QuestionnaireAnswer> result =
                questionnaireAnswerRepo.findByClientIdAndQuestionId(client.getId(), question.getId());

        assertTrue(result.isPresent());
        assertEquals(savedAnswer.getId(), result.get().getId());
    }

    @Test void findByClientIdAndQuestionId_returnsEmptyWhenMissing() {
        Optional<QuestionnaireAnswer> result =
                questionnaireAnswerRepo.findByClientIdAndQuestionId(client.getId(), 999L);

        assertTrue(result.isEmpty());
    }

    @Test void findAllByClientId_returnsAllAnswersForClient() {
        List<QuestionnaireAnswer> answers = questionnaireAnswerRepo.findAllByClientId(client.getId());

        assertEquals(1, answers.size());
        assertEquals(savedAnswer.getId(), answers.getFirst().getId());
    }
}
