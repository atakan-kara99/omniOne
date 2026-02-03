package app.omniOne.controller.client;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.token.JwtFilter;
import app.omniOne.model.dto.NutritionPlanResponse;
import app.omniOne.model.entity.NutritionPlan;
import app.omniOne.model.mapper.NutritionPlanMapper;
import app.omniOne.service.NutritionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import app.omniOne.exception.ProblemDetailFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static app.omniOne.TestFixtures.nutritionPlan;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ClientNutritionPlanController.class)
@Import(ProblemDetailFactory.class)
class ClientNutritionPlanControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private NutritionPlanMapper nutritionPlanMapper;
    @MockitoBean private NutritionPlanService nutritionPlanService;

    private UUID clientId;

    @BeforeEach void setUp() {
        clientId = UUID.randomUUID();
        mockAuthenticatedUser(clientId);
    }

    @Test void getActiveNutriPlan_returnsMappedResponse() throws Exception {
        NutritionPlan plan = nutritionPlan(null);
        NutritionPlanResponse response = new NutritionPlanResponse(
                1600.0, 180.0, 140.0, 60.0, 2.5, 1.0, 7.0,
                LocalDateTime.of(2025, 1, 1, 10, 0));
        when(nutritionPlanService.getActiveNutriPlan(clientId)).thenReturn(plan);
        when(nutritionPlanMapper.map(plan)).thenReturn(response);

        mockMvc.perform(get("/client/nutri-plans/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(1600.0))
                .andExpect(jsonPath("$.proteins").value(140.0));

        verify(nutritionPlanService).getActiveNutriPlan(clientId);
        verify(nutritionPlanMapper).map(plan);
    }

    @Test void getNutriPlans_returnsMappedList() throws Exception {
        NutritionPlan plan1 = nutritionPlan(null);
        NutritionPlan plan2 = nutritionPlan(null);
        NutritionPlanResponse response1 = new NutritionPlanResponse(
                1500.0, 170.0, 130.0, 55.0, 2.0, 1.0, 6.0,
                LocalDateTime.of(2025, 2, 2, 9, 0));
        NutritionPlanResponse response2 = new NutritionPlanResponse(
                1550.0, 175.0, 135.0, 58.0, 2.1, 1.1, 6.5,
                LocalDateTime.of(2025, 2, 3, 9, 0));

        when(nutritionPlanService.getNutriPlans(clientId)).thenReturn(List.of(plan1, plan2));
        when(nutritionPlanMapper.map(plan1)).thenReturn(response1);
        when(nutritionPlanMapper.map(plan2)).thenReturn(response2);

        mockMvc.perform(get("/client/nutri-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].calories").value(1500.0))
                .andExpect(jsonPath("$[1].calories").value(1550.0));

        verify(nutritionPlanService).getNutriPlans(clientId);
        verify(nutritionPlanMapper).map(plan1);
        verify(nutritionPlanMapper).map(plan2);
    }
}
