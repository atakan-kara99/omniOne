package app.omniOne.service;

import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.dto.ChangePasswordRequest;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.*;
import app.omniOne.model.enums.Gender;
import app.omniOne.model.enums.UserRole;
import app.omniOne.model.mapper.UserMapper;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.CoachingRepo;
import app.omniOne.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final CoachRepo coachRepo;
    private final ClientRepo clientRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final CoachingRepo coachingRepo;

    public User getUser(UUID id) {
        log.debug("Trying to retrieve User {}", id);
        User user = userRepo.findByIdOrThrow(id);
        log.info("Successfully retrieved User");
        return user;
    }

    public User changePassword(UUID id, ChangePasswordRequest request) {
        log.debug("Trying to change password for User {}", id);
        User user = userRepo.findByIdOrThrow(id);
        if (!encoder.matches(request.oldPassword(), user.getPassword()))
            throw new NotAllowedException("Old password is incorrect");
        user.setPassword(encoder.encode(request.newPassword()));
        User savedUser = userRepo.save(user);
        log.info("Successfully changed password");
        return savedUser;
    }

    public UserProfile putProfile(UUID id, UserProfileRequest request) {
        log.debug("Trying to update UserProfile for User {}", id);
        User user = userRepo.findByIdOrThrow(id);
        UserProfile profile;
        if (user.getProfile() == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        } else {
            profile = user.getProfile();
        }
        userMapper.map(request, profile);
        UserProfile savedUserProfile = userRepo.save(user).getProfile();
        log.info("Successfully updated UserProfile");
        return savedUserProfile;
    }

    public UserProfile getProfile(UUID id) {
        log.debug("Trying to retrieve UserProfile for User {}", id);
        User user = userRepo.findByIdOrThrow(id);
        UserProfile profile = user.getProfile();
        log.info("Successfully retrieved UserProfile");
        return profile;
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        log.debug("Trying to soft delete User {}", id);
        User user = userRepo.findByIdOrThrow(id);
        if (user.isDeleted())
            throw new NotAllowedException("User already deleted");
        LocalDateTime now = LocalDateTime.now();
        user.setDeleted(true);
        user.setDeletedAt(now);
        user.setEmail(UUID.randomUUID() + "@deleted.user");
        user.setPassword(encoder.encode(UUID.randomUUID().toString()));
        user.getProfile().setGender(Gender.OTHER);
        user.getProfile().setBirthDate(LocalDate.of(1970, 1, 1));
        user.getProfile().setFirstName("deleted");
        user.getProfile().setLastName("user");
        if (user.getRole() == UserRole.COACH) {
            UUID coachId = user.getId();
            List<Coaching> coachings = coachingRepo.findAllByCoachId(coachId);
            coachings.forEach(c -> c.setEndDate(now));
            Coach coach = coachRepo.findByIdOrThrow(coachId);
            coach.getClients().forEach(c -> c.setCoach(null));
        }
        if (user.getRole() == UserRole.CLIENT) {
            UUID clientId = user.getId();
            Client client = clientRepo.findByIdOrThrow(clientId);
            Coaching coaching = coachingRepo.findByCoachIdAndClientIdOrThrow(client.getCoach().getId(), clientId);
            coaching.setEndDate(now);
            client.setCoach(null);
        }
        log.info("Successfully soft deleted User and removed Coaching associations");
    }

}
