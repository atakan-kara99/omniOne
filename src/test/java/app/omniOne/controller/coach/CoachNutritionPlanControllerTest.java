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
                200.0, 150.0, 70.0, 2.0, 1.0, 5.0);
        NutritionPlan plan = nutritionPlan(null);
        NutritionPlanResponse response = new NutritionPlanResponse(
                1800.0, 200.0, 150.0, 70.0, 2.0, 1.0, 5.0,
                LocalDateTime.of(2025, 1, 1, 10, 0));

        when(nutritionPlanService.addNutriPlan(eq(clientId), any(NutritionPlanRequest.class))).thenReturn(plan);
        when(nutritionPlanMapper.map(plan)).thenReturn(response);

        mockMvc.perform(post("/coach/clients/{clientId}/nutri-plans", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(1800.0))
                .andExpect(jsonPath("$.proteins").value(150.0));

        ArgumentCaptor<NutritionPlanRequest> captor = ArgumentCaptor.forClass(NutritionPlanRequest.class);
        verify(nutritionPlanService).addNutriPlan(eq(clientId), captor.capture());
        assertEquals(200.0, captor.getValue().carbs());
        verify(nutritionPlanMapper).map(plan);
    }

    @Test void correctNutriPlan_updatesPlan() throws Exception {
        long planId = 5L;
        NutritionPlanRequest request = new NutritionPlanRequest(
                220.0, 160.0, 80.0, 3.0, 1.5, 6.0);
        NutritionPlan plan = nutritionPlan(null);
        NutritionPlanResponse response = new NutritionPlanResponse(
                1900.0, 220.0, 160.0, 80.0, 3.0, 1.5, 6.0,
                LocalDateTime.of(2025, 2, 2, 9, 30));

        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        when(nutritionPlanService.correctNutriPlan(eq(clientId), eq(planId), any(NutritionPlanRequest.class)))
                .thenReturn(plan);
        when(nutritionPlanMapper.map(plan)).thenReturn(response);

        mockMvc.perform(put("/coach/clients/{clientId}/nutri-plans/{planId}", clientId, planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(1900.0))
                .andExpect(jsonPath("$.fats").value(80.0));

        verify(nutritionPlanService).correctNutriPlan(eq(clientId), eq(planId), any(NutritionPlanRequest.class));
        verify(nutritionPlanMapper).map(plan);
    }

    @Test void getActiveNutriPlan_returnsMappedResponse() throws Exception {
        NutritionPlan plan = new NutritionPlan();
        NutritionPlanResponse response = new NutritionPlanResponse(
                1600.0, 180.0, 140.0, 60.0, 2.5, 1.2, 7.0,
                LocalDateTime.of(2025, 3, 3, 7, 15));

        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        when(nutritionPlanService.getActiveNutriPlan(clientId)).thenReturn(plan);
        when(nutritionPlanMapper.map(plan)).thenReturn(response);

        mockMvc.perform(get("/coach/clients/{clientId}/nutri-plans/active", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(1600.0))
                .andExpect(jsonPath("$.water").value(2.5));

        verify(nutritionPlanService).getActiveNutriPlan(clientId);
        verify(nutritionPlanMapper).map(plan);
    }

    @Test void getNutriPlans_returnsMappedList() throws Exception {
        NutritionPlan plan1 = nutritionPlan(null);
        NutritionPlan plan2 = nutritionPlan(null);
        NutritionPlanResponse response1 = new NutritionPlanResponse(
                1500.0, 170.0, 130.0, 55.0, 2.0, 1.0, 6.0,
                LocalDateTime.of(2025, 4, 4, 6, 0));
        NutritionPlanResponse response2 = new NutritionPlanResponse(
                1550.0, 175.0, 135.0, 58.0, 2.1, 1.1, 6.5,
                LocalDateTime.of(2025, 4, 5, 6, 0));

        when(authService.isCoachedByMe(clientId)).thenReturn(true);
        when(nutritionPlanService.getNutriPlans(clientId)).thenReturn(List.of(plan1, plan2));
        when(nutritionPlanMapper.map(plan1)).thenReturn(response1);
        when(nutritionPlanMapper.map(plan2)).thenReturn(response2);

        mockMvc.perform(get("/coach/clients/{clientId}/nutri-plans", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].calories").value(1500.0))
                .andExpect(jsonPath("$[1].calories").value(1550.0));

        verify(nutritionPlanService).getNutriPlans(clientId);
        verify(nutritionPlanMapper).map(plan1);
        verify(nutritionPlanMapper).map(plan2);
    }
}
