package app.omniOne.authentication;

import app.omniOne.authentication.model.LoginRequest;
import app.omniOne.authentication.model.RegisterRequest;
import app.omniOne.authentication.token.JwtService;
import app.omniOne.email.EmailService;
import app.omniOne.model.enums.UserRole;
import app.omniOne.repository.UserRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static app.omniOne.TestFixtures.coachEmail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepo userRepo;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private EmailService emailService;

    @Test void coach_registers_activates_logs_in_and_fetches_user() throws Exception {
        String password = "Passw0rd!";

        JsonNode registerJson = objectMapper.readTree(
                mockMvc.perform(post("/auth/account/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(
                                        new RegisterRequest(coachEmail, password, UserRole.COACH))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray());

        UUID userId = UUID.fromString(registerJson.get("id").asText());
        assertFalse(userRepo.findByIdOrThrow(userId).isEnabled(), "User should be disabled before activation");

        String activationToken = jwtService.createActivationJwt(coachEmail);
        mockMvc.perform(get("/auth/account/activate").param("token", activationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
        assertTrue(userRepo.findByIdOrThrow(userId).isEnabled(), "User should be enabled after activation");

        JsonNode loginJson = objectMapper.readTree(
                mockMvc.perform(post("/auth/account/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(new LoginRequest(coachEmail, password))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.jwt").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray());

        String bearerToken = "Bearer " + loginJson.get("jwt").asText();

        mockMvc.perform(get("/user").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(coachEmail))
                .andExpect(jsonPath("$.role").value("COACH"));
    }

}
