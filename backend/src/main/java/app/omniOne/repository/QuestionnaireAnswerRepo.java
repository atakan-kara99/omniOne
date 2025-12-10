package app.omniOne.repository;

import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionnaireAnswerRepo extends JpaRepository<QuestionnaireAnswer, Long> {

    boolean existsByClientIdAndQuestionId(UUID clientId, Long questionId);

    Optional<QuestionnaireAnswer> findByClientIdAndQuestionId(UUID clientId, Long id);

    List<QuestionnaireAnswer> findAllByClientId(UUID clientId);

}
