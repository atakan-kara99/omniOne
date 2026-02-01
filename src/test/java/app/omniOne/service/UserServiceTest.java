package app.omniOne.service;

import app.omniOne.authentication.model.dto.ChangePasswordRequest;
import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.entity.User;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.CoachingRepo;
import app.omniOne.authentication.token.RefreshTokenRepo;
import app.omniOne.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static app.omniOne.TestFixtures.user;
import static app.omniOne.TestFixtures.userEmail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) class UserServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private CoachRepo coachRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private CoachingRepo coachingRepo;
    @Mock private RefreshTokenRepo refreshTokenRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach void setUp() {
        userId = UUID.randomUUID();
        user = user(userId);
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

    @Test void softDeleteUser_marksDeletedAndAnonymizes() {
        user.setEmail(userEmail);
        user.setPassword("password");
        user.setDeleted(false);

        when(userRepo.findByIdOrThrow(userId)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("encoded-random");
        when(refreshTokenRepo.findByUserId(user.getId())).thenReturn(java.util.Optional.empty());

        userService.softDeleteUser(userId);

        assertTrue(user.isDeleted());
        assertNotNull(user.getDeletedAt());
        assertTrue(user.getEmail().endsWith("@deleted.user"));
        assertEquals("encoded-random", user.getPassword());
        verify(userRepo).findByIdOrThrow(userId);
        verify(passwordEncoder).encode(any());
        verifyNoMoreInteractions(userRepo);
    }
}
