package app.omniOne.controller;

import app.omniOne.model.dto.ReferenceOptionDto;
import app.omniOne.service.ReferenceDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Reference Data")
@RequiredArgsConstructor
@RequestMapping("/reference")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @GetMapping("/countries")
    @ResponseStatus(HttpStatus.OK)
    public List<ReferenceOptionDto> getCountries(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "100") int limit) {
        return referenceDataService.searchCountries(query, limit);
    }

    @GetMapping("/countries/{countryCode}/cities")
    @ResponseStatus(HttpStatus.OK)
    public List<ReferenceOptionDto> getCities(
            @PathVariable String countryCode,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "100") int limit) {
        return referenceDataService.searchCities(countryCode, query, limit);
    }
}
