package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NutritionPlanRepoTest extends RepositoryTestBase {

    @Autowired private NutritionPlanRepo nutritionPlanRepo;

    private Client client;
    private NutritionPlan recentPlan;
    private NutritionPlan olderPlan;

    @BeforeEach void setUp() {
        Coach coach = persistCoach(persistUser("coach@omni.one", UserRole.COACH));
        client = persistClient(persistUser("client@omni.one", UserRole.CLIENT), coach);
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
        NoSuchResourceException exception = assertThrows(
                NoSuchResourceException.class,
                () -> nutritionPlanRepo.findByIdAndClientIdOrThrow(999L, client.getId()));

        assertEquals("NutritionPlan not found", exception.getMessage());
    }

    @Test void findFirstByClientIdOrderByCreatedAtDescOrThrow_returnsLatestPlan() {
        NutritionPlan result = nutritionPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(client.getId());

        assertEquals(recentPlan.getId(), result.getId());
    }

    @Test void findFirstByClientIdOrderByCreatedAtDescOrThrow_throwsWhenMissing() {
        NoSuchResourceException exception = assertThrows(
                NoSuchResourceException.class,
                () -> nutritionPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(UUID.randomUUID()));

        assertEquals("NutritionPlan not found", exception.getMessage());
    }

    @Test void findByClientIdOrderByCreatedAtDescOrThrow_returnsPlansInOrder() {
        List<NutritionPlan> plans = nutritionPlanRepo.findByClientIdOrderByCreatedAtDescOrThrow(client.getId());

        assertEquals(2, plans.size());
        assertEquals(recentPlan.getId(), plans.get(0).getId());
        assertEquals(olderPlan.getId(), plans.get(1).getId());
    }

    @Test void findByClientIdOrderByCreatedAtDescOrThrow_returnsEmptyListWhenNoPlans() {
        Client otherClient = persistClient(persistUser("client2@omni.one", UserRole.CLIENT), null);
        flushAndClear();

        List<NutritionPlan> plans = nutritionPlanRepo.findByClientIdOrderByCreatedAtDescOrThrow(otherClient.getId());

        assertTrue(plans.isEmpty());
    }
}
