package app.omniOne.controller;

import app.omniOne.authentication.token.JwtFilter;
import app.omniOne.exception.ProblemDetailFactory;
import app.omniOne.model.dto.ReferenceOptionDto;
import app.omniOne.service.ReferenceDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ReferenceDataController.class)
@Import(ProblemDetailFactory.class)
@AutoConfigureMockMvc(addFilters = false)
class ReferenceDataControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private ReferenceDataService referenceDataService;

    @Test void getCountries_returnsSearchResults() throws Exception {
        when(referenceDataService.searchCountries("ger", 20))
                .thenReturn(List.of(new ReferenceOptionDto("DE", "Germany")));

        mockMvc.perform(get("/reference/countries")
                        .param("query", "ger")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("DE"))
                .andExpect(jsonPath("$[0].label").value("Germany"));
    }

    @Test void getCities_returnsSearchResults() throws Exception {
        when(referenceDataService.searchCities("DE", "ber", 20))
                .thenReturn(List.of(new ReferenceOptionDto("Berlin", "Berlin")));

        mockMvc.perform(get("/reference/countries/DE/cities")
                        .param("query", "ber")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("Berlin"))
                .andExpect(jsonPath("$[0].label").value("Berlin"));
    }
}
