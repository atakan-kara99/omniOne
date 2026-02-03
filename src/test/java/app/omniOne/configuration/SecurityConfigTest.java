package app.omniOne.configuration;

import app.omniOne.authentication.token.JwtService;
import app.omniOne.exception.ProblemDetailAccessDeniedHandler;
import app.omniOne.exception.ProblemDetailAuthenticationEntryPoint;
import app.omniOne.exception.ProblemDetailFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static app.omniOne.TestFixtures.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import({SecurityConfig.class, ProblemDetailFactory.class, ProblemDetailAuthenticationEntryPoint.class,
        ProblemDetailAccessDeniedHandler.class})
@WebMvcTest(TestController.class)
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean JwtService jwtService;

    @Test void publicEndpoints_areAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/ping"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/docs/ping"))
                .andExpect(status().isOk());
    }

    @Test void clientEndpoints_requireClientRole() throws Exception {
        mockMvc.perform(get("/client/area"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/client/area").with(user(coachEmail).roles("COACH")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/client/area").with(user(clientEmail).roles("CLIENT")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/client/area").with(user(adminEmail).roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test void coachEndpoints_requireCoachRole() throws Exception {
        mockMvc.perform(get("/coach/area"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/coach/area").with(user(coachEmail).roles("COACH")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/coach/area").with(user(clientEmail).roles("CLIENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/coach/area").with(user(adminEmail).roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test void userEndpoints_allowCoachOrClient() throws Exception {
        mockMvc.perform(get("/user/area"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/user/area").with(user(coachEmail).roles("COACH")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/user/area").with(user(clientEmail).roles("CLIENT")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/user/area").with(user(adminEmail).roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test void adminEndpoints_requireAdminRole() throws Exception {
        mockMvc.perform(get("/admin/area"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/area").with(user(coachEmail).roles("COACH")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/area").with(user(clientEmail).roles("CLIENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/area").with(user(adminEmail).roles("ADMIN")))
                .andExpect(status().isOk());
    }

}

@RestController class TestController {
    @GetMapping("/auth/ping")   @ResponseStatus(OK) String auth()   { return "auth";   }
    @GetMapping("/docs/ping")   @ResponseStatus(OK) String docs()   { return "docs";   }
    @GetMapping("/client/area") @ResponseStatus(OK) String client() { return "client"; }
    @GetMapping("/coach/area" ) @ResponseStatus(OK) String coach()  { return "coach";  }
    @GetMapping("/user/area")   @ResponseStatus(OK) String user()   { return "user";   }
    @GetMapping("/admin/area")  @ResponseStatus(OK) String admin()  { return "admin";  }
}
