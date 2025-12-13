package app.omniOne.service;

import app.omniOne.model.dto.NutritionPlanRequest;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.NutritionPlanRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class NutritionPlanServiceTest {

    @Mock private ClientRepo clientRepo;
    @Mock private NutritionPlanRepo nutritionPlanRepo;
    @Mock private NutritionPlanMapper nutritionPlanMapper;
    @InjectMocks private NutritionPlanService nutritionPlanService;

    private UUID clientId;
    private Client client;

    @BeforeEach void setUp() {
        clientId = UUID.randomUUID();
        client = new Client();
        client.setId(clientId);
    }

    @Test void addNutriPlan_mapsAndSavesNewPlan() {
        NutritionPlanRequest request = new NutritionPlanRequest(
                150.0, 100.0, 50.0, 3.0, 2.0, 5.0);
        NutritionPlan savedPlan = new NutritionPlan();
        savedPlan.setId(1L);

        when(clientRepo.findByIdOrThrow(clientId)).thenReturn(client);
        when(nutritionPlanRepo.save(any(NutritionPlan.class))).thenReturn(savedPlan);

        NutritionPlan result = nutritionPlanService.addNutriPlan(clientId, request);

        assertSame(savedPlan, result);

        ArgumentCaptor<NutritionPlan> planCaptor = ArgumentCaptor.forClass(NutritionPlan.class);
        verify(nutritionPlanMapper).map(eq(request), planCaptor.capture());
        NutritionPlan mappedPlan = planCaptor.getValue();

        ArgumentCaptor<NutritionPlan> savedCaptor = ArgumentCaptor.forClass(NutritionPlan.class);
        verify(nutritionPlanRepo).save(savedCaptor.capture());

        assertSame(mappedPlan, savedCaptor.getValue());
        assertSame(client, mappedPlan.getClient());
        verify(clientRepo).findByIdOrThrow(clientId);
    }

    @Test void getActiveNutriPlan_returnsLatestPlan() {
        NutritionPlan activePlan = new NutritionPlan();
        activePlan.setId(2L);
        when(nutritionPlanRepo.findFirstByClientIdOrderByCreatedAtDescOrThrow(clientId)).thenReturn(activePlan);

        NutritionPlan result = nutritionPlanService.getActiveNutriPlan(clientId);

        assertSame(activePlan, result);
        verify(nutritionPlanRepo).findFirstByClientIdOrderByCreatedAtDescOrThrow(clientId);
        verifyNoInteractions(clientRepo, nutritionPlanMapper);
    }

    @Test void getNutriPlans_returnsPlansForClient() {
        List<NutritionPlan> plans = List.of(new NutritionPlan(), new NutritionPlan());

        when(nutritionPlanRepo.findByClientIdOrderByCreatedAtDescOrThrow(clientId)).thenReturn(plans);

        List<NutritionPlan> result = nutritionPlanService.getNutriPlans(clientId);

        assertEquals(plans, result);
        verify(clientRepo).findByIdOrThrow(clientId);
        verify(nutritionPlanRepo).findByClientIdOrderByCreatedAtDescOrThrow(clientId);
        verifyNoInteractions(nutritionPlanMapper);
    }

    @Test void correctNutriPlan_mapsAndSavesExistingPlan() {
        Long planId = 5L;
        NutritionPlanRequest request = new NutritionPlanRequest(
                120.0, 80.0, 40.0, null, null, null);
        NutritionPlan existingPlan = new NutritionPlan();
        existingPlan.setId(planId);
        NutritionPlan savedPlan = new NutritionPlan();
        savedPlan.setId(planId);

        when(nutritionPlanRepo.findByIdAndClientIdOrThrow(planId, clientId)).thenReturn(existingPlan);
        when(nutritionPlanRepo.save(existingPlan)).thenReturn(savedPlan);

        NutritionPlan result = nutritionPlanService.correctNutriPlan(clientId, planId, request);

        assertSame(savedPlan, result);
        verify(clientRepo).findByIdOrThrow(clientId);
        verify(nutritionPlanRepo).findByIdAndClientIdOrThrow(planId, clientId);
        verify(nutritionPlanMapper).map(request, existingPlan);
        verify(nutritionPlanRepo).save(existingPlan);
    }
}
