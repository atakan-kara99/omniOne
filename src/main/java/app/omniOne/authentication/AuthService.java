package app.omniOne.authentication;

import app.omniOne.authentication.jwt.JwtService;
import app.omniOne.authentication.model.PasswordRequest;
import app.omniOne.authentication.model.RegisterRequest;
import app.omniOne.authentication.model.UserDetails;
import app.omniOne.email.EmailService;
import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.User;
import app.omniOne.model.enums.UserRole;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.UserRepo;
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

    public boolean isCoachedByMe(UUID clientId) {
        Client client = clientRepo.findByIdOrThrow(clientId);
        return client.getCoach().getId().equals(getMyId());
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
