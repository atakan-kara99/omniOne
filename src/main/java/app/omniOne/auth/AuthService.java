package app.omniOne.auth;

import app.omniOne.auth.jwt.JwtService;
import app.omniOne.auth.model.PasswordRequest;
import app.omniOne.auth.model.RegisterRequest;
import app.omniOne.auth.model.UserDetails;
import app.omniOne.email.EmailService;
import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.User;
import app.omniOne.model.enums.UserRole;
import app.omniOne.repo.ClientRepo;
import app.omniOne.repo.CoachRepo;
import app.omniOne.repo.UserRepo;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final CoachRepo coachRepo;
    private final ClientRepo clientRepo;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public static UserDetails getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetails) auth.getPrincipal();
    }

    public static UUID getMyId() {
        return getMe().getId();
    }

    @Transactional
    public User register(RegisterRequest dto) {
        String email = dto.email().trim().toLowerCase();
        if (dto.role().equals(UserRole.ADMIN))
            throw new NotAllowedException("ADMIN registration is not allowed");
        if (userRepo.existsByEmail(email))
            throw new DuplicateResourceException("User already exists");
        User user = userRepo.save(
                User.builder().email(email).password(encoder.encode(dto.password())).role(dto.role()).build());
        if (dto.role().equals(UserRole.COACH))
            coachRepo.save(new Coach(user.getId()));
        if (dto.role().equals(UserRole.CLIENT))
            clientRepo.save(new Client(user.getId()));
        String jwt = jwtService.createActivationJwt(email);
        emailService.sendActivationMail(email, jwt);
        return user;
    }

    public User activate(String token) {
        DecodedJWT jwt = jwtService.verifyActivation(token);
        User user = userRepo.findByEmail(jwt.getClaim("email").asString())
                .orElseThrow(() -> new NoSuchResourceException("User not found"));
        user.setEnabled(true);
        return userRepo.save(user);
    }

    public void sendActivationMail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchResourceException("User not found"));
        if (user.isEnabled())
            throw new NotAllowedException("User already activated");
        String jwt = jwtService.createActivationJwt(email);
        emailService.sendActivationMail(email, jwt);
    }

    public void sendInvitationMail(String clientMail, UUID coachId) {
        if (!userRepo.existsById(coachId))
            throw new NoSuchResourceException("Coach not found");
        String jwt = jwtService.createInvitationJwt(clientMail, coachId);
        emailService.sendInvitationMail(clientMail, jwt);
    }

    @Transactional
    public User acceptInvitation(String token, PasswordRequest request) {
        DecodedJWT jwt = jwtService.verifyInvitation(token);
        UUID coachId = UUID.fromString(jwt.getClaim("coachId").asString());
        Coach coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new NoSuchResourceException("Coach not found"));
        User user = register(new RegisterRequest(
                jwt.getClaim("clientEmail").asString(), request.password(), UserRole.CLIENT));
        Client client = clientRepo.findById(user.getId())
                .orElseThrow(() -> new NoSuchResourceException("Client not found"));
        client.setCoach(coach);
        clientRepo.save(client);
        return user;
    }

    public void sendForgotMail(String email) {
        if (!userRepo.existsByEmail(email))
            throw new NoSuchResourceException("User not found");
        String jwt = jwtService.createResetPasswordJwt(email);
        emailService.sendResetPasswordMail(email, jwt);
    }

    public User reset(String token, PasswordRequest request) {
        DecodedJWT jwt = jwtService.verifyActivation(token);
        User user = userRepo.findByEmail(jwt.getClaim("email").asString())
                .orElseThrow(() -> new NoSuchResourceException("User not found"));
        user.setPassword(encoder.encode(request.password()));
        return userRepo.save(user);
    }
}
