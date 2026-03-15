package app.omniOne.controller.coach;

import app.omniOne.authentication.AuthService;
import app.omniOne.authentication.token.JwtFilter;
import app.omniOne.model.dto.NutritionPlanRequest;
import app.omniOne.model.dto.NutritionPlanResponse;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import app.omniOne.exception.ProblemDetailFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static app.omniOne.TestFixtures.nutritionPlan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CoachNutritionPlanController.class)
@Import(ProblemDetailFactory.class)
class CoachNutritionPlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private AuthService authService;
    @MockitoBean private NutritionPlanMapper nutritionPlanMapper;
    @MockitoBean private NutritionPlanService nutritionPlanService;

    private UUID clientId;

    @BeforeEach void setUp() {
        clientId = UUID.randomUUID();
        when(authService.isCoachedByMe(clientId)).thenReturn(true);
    }

    @Test void addNutriPlan_createsPlanAndReturnsResponse() throws Exception {
        NutritionPlanRequest request = new NutritionPlanRequest(
                200, 150, 70, 2000, 1.0f, 5.0f);
        NutritionPlan plan = nutritionPlan(null);
        NutritionPlanResponse response = new NutritionPlanResponse(
                null, 1800, 200, 150, 70, 2000, 1.0f, 5.0f,
                LocalDateTime.of(2025, 1, 1, 10, 0));

        when(nutritionPlanService.addNutriPlan(eq(clientId), any(NutritionPlanRequest.class))).thenReturn(plan);
        when(nutritionPlanMapper.map(plan)).thenReturn(response);

        mockMvc.perform(post("/coach/clients/{clientId}/nutri-plans", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(1800))
                .andExpect(jsonPath("$.proteins").value(150));

        ArgumentCaptor<NutritionPlanRequest> captor = ArgumentCaptor.forClass(NutritionPlanRequest.class);
        verify(nutritionPlanService).addNutriPlan(eq(clientId), captor.capture());
        assertEquals(200, captor.getValue().carbs());
        verify(nutritionPlanMapper).map(plan);
    }

    @Test void correctNutriPlan_updatesPlan() throws Exception {
        long planId = 5L;
        NutritionPlanRequest request = new NutritionPlanRequest(
                220, 160, 80, 3000, 1.5f, 6.0f);
        NutritionPlan plan = nutritionPlan(null);
        NutritionPlanResponse response = new NutritionPlanResponse(
                null, 1900, 220, 160, 80, 3000, 1.5f, 6.0f,
                LocalDateTime.of(2025, 2, 2, 9, 30));

        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        when(nutritionPlanService.correctNutriPlan(eq(clientId), eq(planId), any(NutritionPlanRequest.class)))
                .thenReturn(plan);
        when(nutritionPlanMapper.map(plan)).thenReturn(response);

        mockMvc.perform(put("/coach/clients/{clientId}/nutri-plans/{planId}", clientId, planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(1900))
                .andExpect(jsonPath("$.fats").value(80));

        verify(nutritionPlanService).correctNutriPlan(eq(clientId), eq(planId), any(NutritionPlanRequest.class));
        verify(nutritionPlanMapper).map(plan);
    }

    @Test void getActiveNutriPlan_returnsMappedResponse() throws Exception {
        NutritionPlan plan = new NutritionPlan();
        NutritionPlanResponse response = new NutritionPlanResponse(
                null, 1600, 180, 140, 60, 2500, 1.2f, 7.0f,
                LocalDateTime.of(2025, 3, 3, 7, 15));

        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        when(nutritionPlanService.getActiveNutriPlan(clientId)).thenReturn(plan);
        when(nutritionPlanMapper.map(plan)).thenReturn(response);

        mockMvc.perform(get("/coach/clients/{clientId}/nutri-plans/active", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(1600))
                .andExpect(jsonPath("$.water").value(2500));

        verify(nutritionPlanService).getActiveNutriPlan(clientId);
        verify(nutritionPlanMapper).map(plan);
    }

    @Test void getNutriPlans_returnsMappedList() throws Exception {
        NutritionPlan plan1 = nutritionPlan(null);
        NutritionPlan plan2 = nutritionPlan(null);
        NutritionPlanResponse response1 = new NutritionPlanResponse(
                null, 1500, 170, 130, 55, 2000, 1.0f, 6.0f,
                LocalDateTime.of(2025, 4, 4, 6, 0));
        NutritionPlanResponse response2 = new NutritionPlanResponse(
                null, 1550, 175, 135, 58, 2100, 1.1f, 6.5f,
                LocalDateTime.of(2025, 4, 5, 6, 0));

        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        when(nutritionPlanService.getNutriPlans(clientId)).thenReturn(List.of(plan1, plan2));
        when(nutritionPlanMapper.map(plan1)).thenReturn(response1);
        when(nutritionPlanMapper.map(plan2)).thenReturn(response2);

        mockMvc.perform(get("/coach/clients/{clientId}/nutri-plans", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].calories").value(1500))
                .andExpect(jsonPath("$[1].calories").value(1550));

        verify(nutritionPlanService).getNutriPlans(clientId);
        verify(nutritionPlanMapper).map(plan1);
        verify(nutritionPlanMapper).map(plan2);
    }
}
