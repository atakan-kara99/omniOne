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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final CoachRepo coachRepo;
    private final ClientRepo clientRepo;
    private final QuestionnaireAnswerRepo answerRepo;
    private final QuestionnaireQuestionRepo questionRepo;

    public List<QuestionnaireQuestion> getQuestionsForCoach(UUID coachId) {
        log.debug("Trying to retrieve questions for coach {}", coachId);
        List<QuestionnaireQuestion> questions = questionRepo.findAllByCoachIdOrCoachIdIsNull(coachId);
        log.info("Successfully retrieved questions for coach");
        return questions;
    }

    public QuestionnaireQuestion addQuestion(UUID coachId, QuestionnaireQuestionPostRequest request) {
        log.debug("Trying to add question for coach {}", coachId);
        QuestionnaireQuestion question = new QuestionnaireQuestion();
        Coach coach = coachRepo.findByIdOrThrow(coachId);
        question.setText(request.text());
        question.setCoach(coach);
        questionRepo.save(question);
        log.info("Successfully added question");
        return question;
    }

    public void deleteQuestion(UUID coachId, Long questionId) {
        log.debug("Trying to delete question {} for coach {}", questionId, coachId);
        QuestionnaireQuestion question = questionRepo.findByIdAndCoachIdOrThrow(questionId, coachId);
        questionRepo.delete(question);
        log.info("Successfully deleted question");
    }

    public List<QuestionnaireQuestion> getQuestionsForClient(UUID clientId) {
        log.debug("Trying to retrieve questions for client {}", clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        Coach coach = client.getCoachOrThrow();
        List<QuestionnaireQuestion> questions = questionRepo.findAllByCoachIdOrCoachIdIsNull(coach.getId());
        log.info("Successfully retrieved questions for client");
        return questions;
    }

    public void putAnswers(UUID clientId, List<QuestionnaireAnswerRequest> requests) {
        log.debug("Trying to add QuestionnaireAnswers for Client {}", clientId);
        Client client = clientRepo.findByIdOrThrow(clientId);
        List<QuestionnaireAnswer> answers = new ArrayList<>();
        for (QuestionnaireAnswerRequest req : requests) {
            QuestionnaireQuestion question = questionRepo.findByIdAOrThrow(req.questionId());
            if (question.getCoach() != null &&
                    !question.getCoach().getId().equals(client.getCoachOrThrow().getId())) {
                log.info("Question not found for coach (questionId={}, coachId={})",
                        question.getId(), client.getCoachOrThrow().getId());
                throw new NoSuchResourceException("Question not found for coach");
            }
            // Try to find existing answer
            QuestionnaireAnswer answer = answerRepo
                    .findByClientIdAndQuestionId(clientId, question.getId())
                    .orElseGet(() -> {
                        // Create new answer if none exists
                        QuestionnaireAnswer newAnswer = new QuestionnaireAnswer();
                        newAnswer.setClient(client);
                        newAnswer.setQuestion(question);
                        return newAnswer;
                    });
            answer.setAnswer(req.answer());
            answers.add(answer);
        }
        answerRepo.saveAll(answers);
        log.info("Successfully added QuestionnaireAnswers");
    }

    public List<QuestionnaireAnswerResponse> getAnswers(UUID clientId) {
        log.debug("Trying to retrieve QuestionnaireAnswers for client {}", clientId);
        List<QuestionnaireAnswer> answers = answerRepo.findAllByClientId(clientId);
        List<QuestionnaireAnswerResponse> responses = new ArrayList<>();
        for (QuestionnaireAnswer ans : answers) {
            responses.add(new QuestionnaireAnswerResponse(ans.getQuestion().getId(), ans.getQuestion().getText(), ans.getAnswer()));
        }
        log.debug("Successfully retrieved QuestionnaireAnswers");
        return responses;
    }

}
