package app.omniOne.service;

import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.dto.ChangePasswordRequest;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.model.enums.Gender;
import app.omniOne.model.mapper.UserMapper;
import app.omniOne.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class UserServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
    }

    @Test void getUser_returnsUserFromRepository() {
        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);

        User result = userService.getUser(userId);

        assertSame(user, result);
        verify(userRepo).findByIdOrThrow(userId);
    }

    @Test void changePassword_updatesPasswordWhenOldMatches() {
        ChangePasswordRequest request = new ChangePasswordRequest("old", "new");
        user.setPassword("old-hash");
        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setPassword("encoded-new");

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(passwordEncoder.matches("old", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");
        when(userRepo.save(user)).thenReturn(savedUser);

        User result = userService.changePassword(userId, request);

        assertSame(savedUser, result);
        assertEquals("encoded-new", user.getPassword());
        verify(userRepo).findByIdOrThrow(userId);
        verify(passwordEncoder).matches("old", "old-hash");
        verify(passwordEncoder).encode("new");
        verify(userRepo).save(user);
    }

    @Test void changePassword_throwsWhenOldPasswordIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest("old", "new");
        user.setPassword("old-hash");

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(passwordEncoder.matches("old", "old-hash")).thenReturn(false);

        assertThrows(NotAllowedException.class,
                () -> userService.changePassword(userId, request),
                "Expected changePassword to reject incorrect old password");

        verify(userRepo).findByIdOrThrow(userId);
        verify(passwordEncoder).matches("old", "old-hash");
        verify(userRepo, never()).save(any());
    }

    @Test void putProfile_createsProfileWhenMissing() {
        UserProfileRequest request = new UserProfileRequest(
                "John", "Doe", LocalDate.of(1990, 1, 1), Gender.MALE);

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(userRepo.save(user)).thenReturn(user);

        UserProfile result = userService.putProfile(userId, request);

        assertNotNull(result);
        assertSame(user, result.getUser());
        assertSame(result, user.getProfile());
        verify(userRepo).findByIdOrThrow(userId);
        verify(userMapper).map(request, result);
        verify(userRepo).save(user);
    }

    @Test void putProfile_updatesExistingProfile() {
        UserProfileRequest request = new UserProfileRequest(
                "Jane", "Roe", LocalDate.of(1985, 5, 5), Gender.FEMALE);
        UserProfile existingProfile = new UserProfile();
        existingProfile.setUser(user);
        user.setProfile(existingProfile);

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(userRepo.save(user)).thenReturn(user);

        UserProfile result = userService.putProfile(userId, request);

        assertSame(existingProfile, result);
        assertSame(existingProfile, user.getProfile());
        verify(userRepo).findByIdOrThrow(userId);
        verify(userMapper).map(request, existingProfile);
        verify(userRepo).save(user);
    }

    @Test void getProfile_returnsUserProfile() {
        UserProfile profile = new UserProfile();
        user.setProfile(profile);
        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);

        UserProfile result = userService.getProfile(userId);

        assertSame(profile, result);
        verify(userRepo).findByIdOrThrow(userId);
    }

    @Test void softDeleteUser_marksDeletedAndAnonymizes() {
        user.setEmail("user@mail.com");
        user.setPassword("password");
        user.setDeleted(false);
        UserProfile profile = new UserProfile();
        profile.setGender(Gender.MALE);
        profile.setBirthDate(LocalDate.of(1995, 2, 2));
        profile.setFirstName("John");
        profile.setLastName("Doe");
        user.setProfile(profile);

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("encoded-random");

        userService.softDeleteUser(userId);

        assertTrue(user.isDeleted());
        assertNotNull(user.getDeletedAt());
        assertTrue(user.getEmail().endsWith("@deleted.user"));
        assertEquals("encoded-random", user.getPassword());
        assertEquals(Gender.OTHER, user.getProfile().getGender());
        assertEquals(LocalDate.of(1970, 1, 1), user.getProfile().getBirthDate());
        assertEquals("deleted", user.getProfile().getFirstName());
        assertEquals("user", user.getProfile().getLastName());

        verify(userRepo).findByIdOrThrow(userId);
        verify(passwordEncoder).encode(any());
        verifyNoMoreInteractions(userRepo);
    }
}
