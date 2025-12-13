package app.omniOne.repository;

import app.omniOne.model.entity.*;
import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import app.omniOne.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@DataJpaTest
@ActiveProfiles("test")
abstract class RepositoryTestBase {

    @Autowired protected TestEntityManager entityManager;

    protected User persistUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setEnabled(true);
        user.setDeleted(false);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    protected Coach persistCoach(User user) {
        Coach coach = new Coach();
        coach.setId(user.getId());
        coach.setUser(user);
        entityManager.persist(coach);
        return coach;
    }

    protected Client persistClient(User user, Coach coach) {
        Client client = new Client();
        client.setId(user.getId());
        client.setUser(user);
        client.setCoach(coach);
        entityManager.persist(client);
        return client;
    }

    protected Coaching persistCoaching(Coach coach, Client client) {
        Coaching coaching = new Coaching();
        coaching.setCoach(coach);
        coaching.setClient(client);
        entityManager.persist(coaching);
        return coaching;
    }

    protected QuestionnaireQuestion persistQuestion(String text, Coach coach) {
        QuestionnaireQuestion question = new QuestionnaireQuestion();
        question.setText(text);
        question.setCoach(coach);
        entityManager.persist(question);
        return question;
    }

    protected QuestionnaireAnswer persistAnswer(Client client, QuestionnaireQuestion question, String answerText) {
        QuestionnaireAnswer answer = new QuestionnaireAnswer();
        answer.setClient(client);
        answer.setQuestion(question);
        answer.setAnswer(answerText);
        entityManager.persist(answer);
        return answer;
    }

    protected NutritionPlan persistNutritionPlan(
            Client client, double carbs, double proteins, double fats, LocalDateTime createdAt) {
        NutritionPlan plan = new NutritionPlan();
        plan.setClient(client);
        plan.setCarbs(carbs);
        plan.setProteins(proteins);
        plan.setFats(fats);
        entityManager.persist(plan);
        entityManager.flush();
        setCreatedAt(plan.getId(), createdAt);
        return plan;
    }

    private void setCreatedAt(Long planId, LocalDateTime createdAt) {
        entityManager.getEntityManager()
                .createNativeQuery("update nutrition_plan set created_at = ? where id = ?")
                .setParameter(1, Timestamp.valueOf(createdAt))
                .setParameter(2, planId)
                .executeUpdate();
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
