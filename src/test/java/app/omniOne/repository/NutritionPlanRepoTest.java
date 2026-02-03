package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static app.omniOne.TestFixtures.clientEmail;
import static app.omniOne.TestFixtures.coachEmail;
import static org.junit.jupiter.api.Assertions.*;

class NutritionPlanRepoTest extends RepositoryTestBase {

    @Autowired private NutritionPlanRepo nutritionPlanRepo;

    private Client client;
    private NutritionPlan recentPlan;
    private NutritionPlan olderPlan;

    @BeforeEach void setUp() {
        Coach coach = persistCoach(persistUser(coachEmail, UserRole.COACH));
        client = persistClient(persistUser(clientEmail, UserRole.CLIENT), coach);
        olderPlan = persistNutritionPlan(client, 200.0, 100.0, 50.0,
                LocalDateTime.of(2024, 1, 1, 12, 0));
        recentPlan = persistNutritionPlan(client, 220.0, 110.0, 60.0,
                LocalDateTime.of(2024, 6, 1, 12, 0));
        flushAndClear();
    }

    @Test void findByIdAndClientIdOrThrow_returnsPlanWhenPresent() {
        NutritionPlan result = nutritionPlanRepo.findByIdAndClientIdOrThrow(recentPlan.getId(), client.getId());

        assertEquals(recentPlan.getId(), result.getId());
        assertEquals(client.getId(), result.getClient().getId());
    }

    @Test void findByIdAndClientIdOrThrow_throwsWhenMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> nutritionPlanRepo.findByIdAndClientIdOrThrow(999L, client.getId()));

        assertEquals("NutritionPlan not found", exception.getMessage());
    }

    @Test void findFirstByClientIdOrderByCreatedAtDescOrThrow_returnsLatestPlan() {
        NutritionPlan result = nutritionPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(client.getId());

        assertEquals(recentPlan.getId(), result.getId());
    }

    @Test void findFirstByClientIdOrderByCreatedAtDescOrThrow_throwsWhenMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> nutritionPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(UUID.randomUUID()));

        assertEquals("NutritionPlan not found", exception.getMessage());
    }

    @Test void findByClientIdOrderByCreatedAtDescOrThrow_returnsPlansInOrder() {
        List<NutritionPlan> plans = nutritionPlanRepo.findByClientIdOrderByCreatedAtDesc(client.getId());

        assertEquals(2, plans.size());
        assertEquals(recentPlan.getId(), plans.get(0).getId());
        assertEquals(olderPlan.getId(), plans.get(1).getId());
    }

    @Test void findByClientIdOrderByCreatedAtDescOrThrow_returnsEmptyListWhenNoPlans() {
        Client otherClient = persistClient(persistUser("client2@omni.one", UserRole.CLIENT), null);
        flushAndClear();

        List<NutritionPlan> plans = nutritionPlanRepo.findByClientIdOrderByCreatedAtDesc(otherClient.getId());

        assertTrue(plans.isEmpty());
    }
}
