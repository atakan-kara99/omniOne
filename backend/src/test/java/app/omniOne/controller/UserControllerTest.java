package app.omniOne.controller;

import app.omniOne.AuthTestSupport;
import app.omniOne.authentication.model.dto.ChangePasswordRequest;
import app.omniOne.authentication.token.JwtFilter;
import app.omniOne.model.dto.UserDto;
import app.omniOne.model.dto.UserProfileDto;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.model.enums.Gender;
import app.omniOne.model.enums.UserRole;
import app.omniOne.model.mapper.UserMapper;
import app.omniOne.model.mapper.UserProfileMapper;
import app.omniOne.service.UserProfileService;
import app.omniOne.service.UserService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static app.omniOne.TestFixtures.userEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest extends AuthTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private UserMapper userMapper;
    @MockitoBean private UserService userService;
    @MockitoBean private UserProfileMapper userProfileMapper;
    @MockitoBean private UserProfileService userProfileService;

    private UUID userId;

    @BeforeEach void setUp() {
        userId = UUID.randomUUID();
        mockAuthenticatedUser(userId);
    }

    @Test void getUser_returnsMappedDto() throws Exception {
        User user = new User();
        UserDto dto = new UserDto(userId, userEmail, UserRole.CLIENT,
                LocalDateTime.of(2025, 1, 1, 12, 0));

        when(userService.getUser(userId)).thenReturn(user);
        when(userMapper.map(user)).thenReturn(dto);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.updatedAt").value("2025-01-01T12:00:00"));

        verify(userService).getUser(userId);
        verify(userMapper).map(user);
    }

    @Test void changePassword_updatesUserAndReturnsDto() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Testpq12", "Testpq34");
        User user = new User();
        UserDto dto = new UserDto(userId, userEmail, UserRole.COACH,
                LocalDateTime.of(2025, 2, 2, 8, 30));

        when(userService.changePassword(eq(userId), any(ChangePasswordRequest.class))).thenReturn(user);
        when(userMapper.map(user)).thenReturn(dto);

        mockMvc.perform(post("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("COACH"))
                .andExpect(jsonPath("$.updatedAt").value("2025-02-02T08:30:00"));

        ArgumentCaptor<ChangePasswordRequest> captor = ArgumentCaptor.forClass(ChangePasswordRequest.class);
        verify(userService).changePassword(eq(userId), captor.capture());
        ChangePasswordRequest captured = captor.getValue();
        assertEquals("Testpq12", captured.oldPassword());
        assertEquals("Testpq34", captured.newPassword());
        verify(userMapper).map(user);
    }

    @Test void getProfile_returnsMappedProfile() throws Exception {
        UserProfile profile = new UserProfile();
        UserProfileDto dto = new UserProfileDto("John", "Doe",
                LocalDate.of(1990, 1, 1), Gender.MALE);
        when(userProfileService.getProfile(userId)).thenReturn(profile);
        when(userProfileMapper.map(profile)).thenReturn(dto);

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"))
                .andExpect(jsonPath("$.gender").value("MALE"));

        verify(userProfileService).getProfile(userId);
        verify(userProfileMapper).map(profile);
    }

    @Test void putProfile_updatesProfileAndReturnsDto() throws Exception {
        UserProfileRequest request = new UserProfileRequest(
                "Jane", "Roe", LocalDate.of(1995, 5, 5), Gender.FEMALE);
        UserProfile profile = new UserProfile();
        UserProfileDto dto = new UserProfileDto("Jane", "Roe",
                LocalDate.of(1995, 5, 5), Gender.FEMALE);

        when(userProfileService.putProfile(eq(userId), any(UserProfileRequest.class))).thenReturn(profile);
        when(userProfileMapper.map(profile)).thenReturn(dto);

        mockMvc.perform(put("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.gender").value("FEMALE"));

        ArgumentCaptor<UserProfileRequest> captor = ArgumentCaptor.forClass(UserProfileRequest.class);
        verify(userProfileService).putProfile(eq(userId), captor.capture());
        UserProfileRequest captured = captor.getValue();
        assertEquals("Jane", captured.firstName());
        assertEquals(LocalDate.of(1995, 5, 5), captured.birthDate());
        verify(userProfileMapper).map(profile);
    }

    @Test void deleteUser_softDeletesAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/user"))
                .andExpect(status().isNoContent());

        verify(userService).softDeleteUser(userId);
        verifyNoMoreInteractions(userMapper);
    }

    @Test void changePassword_returnsBadRequestOnValidationError() throws Exception {
        ChangePasswordRequest invalid = new ChangePasswordRequest("", "");

        mockMvc.perform(post("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.oldPassword").value("size must be between 8 and 32"))
                .andExpect(jsonPath("$.errors.newPassword").value("size must be between 8 and 32"));

        verifyNoInteractions(userService);
        verifyNoInteractions(userMapper);
    }

}
