package app.omniOne.service;

import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.model.mapper.UserProfileMapper;
import app.omniOne.repository.UserProfileRepo;
import app.omniOne.repository.UserRepo;
import app.omniOne.exception.ErrorCode;
import app.omniOne.exception.custom.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepo userRepo;
    private final UserProfileRepo userProfileRepo;
    private final UserProfileMapper userProfileMapper;
    private final ReferenceDataService referenceDataService;

    public UserProfile getProfile(UUID id) {
        return userProfileRepo.findByIdOrThrow(id);
    }

    @Transactional
    public UserProfile putProfile(UUID id, UserProfileRequest request) {
        validateLocation(request);
        User user = userRepo.findByIdOrThrow(id);
        UserProfile profile = userProfileRepo.findById(id)
                .orElseGet(() -> UserProfile.builder()
                        .user(user).build());
        userProfileMapper.map(request, profile);
        userProfileRepo.save(profile);
        log.info("UserProfile updated (userId={})", id);
        return profile;
    }

    private void validateLocation(UserProfileRequest request) {
        if (!referenceDataService.isValidCountryCode(request.countryCode())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST,
                    "Country is invalid");
        }
        if (!referenceDataService.isValidCity(request.countryCode(), request.city())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST,
                    "City is invalid");
        }
    }

}
