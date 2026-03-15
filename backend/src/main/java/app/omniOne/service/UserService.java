package app.omniOne.service;

import app.omniOne.authentication.model.dto.ChangePasswordRequest;
import app.omniOne.authentication.token.RefreshTokenRepo;
import app.omniOne.exception.custom.OperationNotAllowedException;
import app.omniOne.model.entity.*;
import app.omniOne.model.enums.Gender;
import app.omniOne.model.enums.UserRole;
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
    private final PasswordEncoder encoder;
    private final CoachingRepo coachingRepo;
    private final RefreshTokenRepo refreshTokenRepo;

    public User getUser(UUID id) {
        return userRepo.findByIdOrThrow(id);
    }

    public User changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepo.findByIdOrThrow(id);
        if (!encoder.matches(request.oldPassword(), user.getPassword()))
            throw new OperationNotAllowedException("Old password is incorrect");
        user.setPassword(encoder.encode(request.newPassword()));
        User savedUser = userRepo.save(user);
        log.info("Password changed (userId={})", savedUser.getId());
        return savedUser;
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        User user = userRepo.findByIdOrThrow(id);
        if (user.isDeleted())
            throw new OperationNotAllowedException("User already deleted");
        LocalDateTime now = LocalDateTime.now();
        user.setDeleted(true);
        user.setDeletedAt(now);
        user.setEmail(UUID.randomUUID() + "@deleted.user");
        user.setPassword(encoder.encode(UUID.randomUUID().toString()));
        UserProfile profile = user.getProfile();
        if (profile != null) {
            profile.setGender(Gender.OTHER);
            profile.setBirthDate(LocalDate.of(1970, 1, 1));
            profile.setFirstName("deleted");
            profile.setLastName("user");
        }
        if (user.getRole() == UserRole.COACH) {
            UUID coachId = user.getId();
            List<Coaching> coachings = coachingRepo.findAllByCoachIdAndEndDateIsNull(coachId);
            coachings.forEach(c -> c.setEndDate(now));
            Coach coach = coachRepo.findByIdOrThrow(coachId);
            coach.getClients().forEach(c -> c.setCoach(null));
        }
        if (user.getRole() == UserRole.CLIENT) {
            UUID clientId = user.getId();
            Client client = clientRepo.findByIdOrThrow(clientId);
            Coach coach = client.getCoach();
            if (coach != null) {
                Coaching coaching = coachingRepo.findByClientIdAndEndDateIsNullOrThrow(clientId);
                coaching.setEndDate(now);
                client.setCoach(null);
            }
        }
        refreshTokenRepo.findAllByUserId(user.getId())
                .forEach(rt -> rt.setRevokedAt(LocalDateTime.now()));
        log.info("User soft deleted and coaching associations removed (userId={})", user.getId());
    }

}
