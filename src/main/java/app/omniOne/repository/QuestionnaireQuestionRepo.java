package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionnaireQuestionRepo extends JpaRepository<QuestionnaireQuestion, Long> {

    List<QuestionnaireQuestion> findAllByCoachIdOrCoachIdIsNull(UUID coachId);

    Optional<QuestionnaireQuestion> findByIdAndCoachId(Long questionId, UUID coachId);

    default QuestionnaireQuestion findByIdAndCoachIdOrThrow(Long questionId, UUID coachId) {
        return findByIdAndCoachId(questionId, coachId)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionnaireQuestion not found"));
    }

    default QuestionnaireQuestion findByIdAOrThrow(Long questionId) {
        return findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionnaireQuestion not found"));
    }

}
