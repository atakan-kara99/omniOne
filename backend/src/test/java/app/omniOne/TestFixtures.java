package app.omniOne;

import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;

import java.util.UUID;

/**
 * Small factory helpers to keep test setup concise.
 */
public final class TestFixtures {

    public static String userEmail = "user@omni.one";
    public static String adminEmail = "admin@omni.one";
    public static String coachEmail = "coach@omni.one";
    public static String clientEmail = "client@omni.one";

    private TestFixtures() {}

    public static User user(UUID id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    public static Coach coach(UUID id) {
        Coach coach = new Coach();
        coach.setId(id);
        return coach;
    }

    public static Client client(UUID id) {
        Client client = new Client();
        client.setId(id);
        return client;
    }

    public static Client client(UUID id, Coach coach) {
        Client client = client(id);
        client.setCoach(coach);
        return client;
    }

    public static QuestionnaireQuestion question(long id, Coach coach, String text) {
        QuestionnaireQuestion question = new QuestionnaireQuestion();
        question.setId(id);
        question.setCoach(coach);
        question.setText(text);
        return question;
    }

    public static QuestionnaireAnswer answer(QuestionnaireQuestion question, Client client, String text) {
        QuestionnaireAnswer answer = new QuestionnaireAnswer();
        answer.setQuestion(question);
        answer.setClient(client);
        answer.setAnswer(text);
        return answer;
    }

    public static NutritionPlan nutritionPlan(Long id) {
        NutritionPlan plan = new NutritionPlan();
        plan.setId(id);
        return plan;
    }

}
