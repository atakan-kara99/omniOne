package app.omniOne.service;

import app.omniOne.exception.ErrorCode;
import app.omniOne.exception.custom.ApiException;
import app.omniOne.model.dto.ReferenceOptionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReferenceDataService {

    private record CountryResource(String code, String name) {}

    private static final TypeReference<List<CountryResource>> COUNTRY_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<String>> CITY_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private volatile List<ReferenceOptionDto> countries;
    private final Map<String, List<String>> citiesByCountryCode = new ConcurrentHashMap<>();

    public ReferenceDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ReferenceOptionDto> getCountries() {
        List<ReferenceOptionDto> snapshot = countries;
        if (snapshot != null) {
            return snapshot;
        }
        synchronized (this) {
            if (countries == null) {
                countries = readCountries();
            }
            return countries;
        }
    }

    public List<ReferenceOptionDto> searchCountries(String query, int limit) {
        return getCountries().stream()
                .filter(country -> country != null && country.value() != null && country.label() != null)
                .filter(country -> matches(country.label(), query))
                .limit(normalizeLimit(limit))
                .toList();
    }

    public List<ReferenceOptionDto> searchCities(String countryCode, String query, int limit) {
        return getCities(countryCode).stream()
                .filter(city -> matches(city, query))
                .limit(normalizeLimit(limit))
                .map(city -> new ReferenceOptionDto(city, city))
                .toList();
    }

    public boolean isValidCountryCode(String countryCode) {
        return getCountries().stream()
                .filter(country -> country != null && country.value() != null)
                .anyMatch(country -> country.value().equalsIgnoreCase(countryCode));
    }

    public boolean isValidCity(String countryCode, String city) {
        return getCities(countryCode).stream().anyMatch(existingCity -> existingCity.equals(city));
    }

    private List<String> getCities(String countryCode) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);
        return citiesByCountryCode.computeIfAbsent(normalizedCountryCode, this::readCities);
    }

    private List<ReferenceOptionDto> readCountries() {
        try (InputStream stream = new ClassPathResource("reference/countries.json").getInputStream()) {
            return objectMapper.readValue(stream, COUNTRY_TYPE).stream()
                    .filter(country -> country != null && country.code() != null && country.name() != null)
                    .map(country -> new ReferenceOptionDto(country.code(), country.name()))
                    .toList();
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to load country reference data", ex);
        }
    }

    private List<String> readCities(String countryCode) {
        ClassPathResource resource = new ClassPathResource("reference/cities/" + countryCode + ".json");
        if (!resource.exists()) {
            return List.of();
        }

        try (InputStream stream = resource.getInputStream()) {
            return objectMapper.readValue(stream, CITY_TYPE).stream()
                    .filter(city -> city != null && !city.isBlank())
                    .toList();
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to load city reference data", ex);
        }
    }

    private String normalizeCountryCode(String countryCode) {
        return (countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.ROOT));
    }

    private boolean matches(String value, String query) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (query == null || query.isBlank()) {
            return true;
        }
        return value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private long normalizeLimit(int limit) {
        if (limit <= 0) {
            return 50L;
        }
        return Math.min(limit, 10000L);
    }
}
