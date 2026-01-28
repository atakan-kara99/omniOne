package app.omniOne.service;

import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.model.enums.Gender;
import app.omniOne.model.mapper.UserProfileMapper;
import app.omniOne.repository.UserProfileRepo;
import app.omniOne.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static app.omniOne.TestFixtures.user;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private UserProfileRepo userProfileRepo;
    @Mock private UserProfileMapper userProfileMapper;
    @InjectMocks private UserProfileService userProfileService;

    private UUID userId;
    private User user;

    @BeforeEach void setUp() {
        userId = UUID.randomUUID();
        user = user(userId);
    }

    @Test void getProfile_returnsUserProfile() {
        UserProfile profile = new UserProfile();
        when(userProfileRepo.findByIdOrThrow(userId)).thenReturn(profile);

        UserProfile result = userProfileService.getProfile(userId);

        assertSame(profile, result);
        verify(userProfileRepo).findByIdOrThrow(userId);
    }

    @Test void putProfile_createsProfileWhenMissing() {
        UserProfileRequest request = new UserProfileRequest(
                "John", "Doe", LocalDate.of(1990, 1, 1), Gender.MALE);

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(userProfileRepo.findById(userId)).thenReturn(Optional.empty());

        UserProfile result = userProfileService.putProfile(userId, request);

        assertNotNull(result);
        assertSame(user, result.getUser());
        verify(userProfileMapper).map(request, result);
        verify(userProfileRepo).save(result);
    }

    @Test void putProfile_updatesExistingProfile() {
        UserProfileRequest request = new UserProfileRequest(
                "Jane", "Roe", LocalDate.of(1985, 5, 5), Gender.FEMALE);
        UserProfile existingProfile = new UserProfile();
        existingProfile.setUser(user);

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(userProfileRepo.findById(userId)).thenReturn(Optional.of(existingProfile));

        UserProfile result = userProfileService.putProfile(userId, request);

        assertSame(existingProfile, result);
        verify(userProfileMapper).map(request, existingProfile);
        verify(userProfileRepo).save(existingProfile);
    }

}
