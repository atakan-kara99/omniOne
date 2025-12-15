package app.omniOne.controller.coach;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.AuthService;
import app.omniOne.authentication.jwt.JwtFilter;
import app.omniOne.model.dto.CoachPatchRequest;
import app.omniOne.model.dto.CoachResponse;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.mapper.CoachMapper;
import app.omniOne.service.CoachService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static app.omniOne.TestFixtures.coach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CoachCoachController.class)
@AutoConfigureMockMvc(addFilters = false)
class CoachCoachControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private CoachMapper coachMapper;
    @MockitoBean private CoachService coachService;

    private UUID coachId;

    @BeforeEach void setUp() {
        coachId = UUID.randomUUID();
        mockAuthenticatedUser(coachId);
    }

    @Test void getCoach_returnsMappedResponse() throws Exception {
        Coach coach = coach(coachId);
        CoachResponse response = new CoachResponse(coachId);

        when(coachService.getCoach(coachId)).thenReturn(coach);
        when(coachMapper.map(coach)).thenReturn(response);

        mockMvc.perform(get("/coach"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(coachId.toString()));

        verify(coachService).getCoach(coachId);
        verify(coachMapper).map(coach);
    }

    @Test void patchCoach_updatesAndReturnsResponse() throws Exception {
        CoachPatchRequest request = new CoachPatchRequest("coach@omni.one");
        Coach coach = coach(coachId);
        CoachResponse response = new CoachResponse(coachId);

        when(coachService.patchCoach(eq(coachId), any(CoachPatchRequest.class))).thenReturn(coach);
        when(coachMapper.map(coach)).thenReturn(response);

        mockMvc.perform(patch("/coach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(coachId.toString()));

        ArgumentCaptor<CoachPatchRequest> captor = ArgumentCaptor.forClass(CoachPatchRequest.class);
        verify(coachService).patchCoach(eq(coachId), captor.capture());
        assertEquals("coach@omni.one", captor.getValue().email());
        verify(coachMapper).map(coach);
    }

    @Test void patchCoach_returnsBadRequestWhenEmailInvalid() throws Exception {
        CoachPatchRequest invalid = new CoachPatchRequest("not-an-email");

        mockMvc.perform(patch("/coach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.email")
                        .value("must be a well-formed email address"));

        verifyNoInteractions(coachService);
        verifyNoInteractions(coachMapper);
    }
}
