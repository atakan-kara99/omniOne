package app.omniOne.authentication;

import app.omniOne.authentication.model.*;
import app.omniOne.authentication.token.JwtService;
import app.omniOne.authentication.token.RefreshToken;
import app.omniOne.authentication.token.RefreshTokenService;
import app.omniOne.chatting.repository.ChatParticipantRepo;
import app.omniOne.email.EmailService;
import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.User;
import app.omniOne.model.enums.UserRole;
import app.omniOne.repository.ClientRepo;
import app.omniOne.repository.CoachRepo;
import app.omniOne.repository.CoachingRepo;
import app.omniOne.repository.UserRepo;
import app.omniOne.service.CoachingService;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;

    private final UserRepo userRepo;
    private final CoachRepo coachRepo;
    private final ClientRepo clientRepo;
    private final CoachingRepo coachingRepo;
    private final ChatParticipantRepo chatParticipantRepo;

    private final JwtService jwtService;
    private final EmailService emailService;
    private final CoachingService coachingService;
    private final RefreshTokenService refreshTokenService;

    public static UUID getMyId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public boolean isCoachedByMe(UUID clientId) {
        UUID coachId = getMyId();
        log.debug("Checking if Coach {} has permission to access Client {} info", coachId, clientId);
        return coachingRepo.existsByCoachIdAndClientId(coachId, clientId);
    }

    public boolean isRelated(UUID userId1, UUID userId2) {
        log.debug("Checking if User {} has permission to message User {}", userId1, userId2);
        return coachingRepo.existsByCoachIdAndClientId(userId1, userId2) ||
                coachingRepo.existsByCoachIdAndClientId(userId2, userId1);
    }

    public boolean isChatOf(UUID userId, UUID conversationId) {
        log.debug("Checking if User {} has permission to access ChatConversation {}", userId, conversationId);
        return chatParticipantRepo.existsByConversationIdAndUserId(conversationId, userId);
    }

    public LoginResponse login(LoginRequest request) {
        log.debug("Trying to log in User {}", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.createAuthJwt(userDetails);
        String token = refreshTokenService.generateToken();
        refreshTokenService.saveRefreshToken(token, userDetails.getUser());
        log.info("Successfully logged in");
        return new LoginResponse(jwt, token);
    }

    public LoginResponse refreshTokens(String rawToken) {
        log.debug("Trying to refresh jwt for User");
        RefreshToken refreshToken = refreshTokenService.getRefreshToken(rawToken);
        UserDetails userDetails = new UserDetails(refreshToken.getUser());
        String jwt = jwtService.createAuthJwt(userDetails);
        String newToken = refreshTokenService.rotateRefreshToken(rawToken);
        log.info("Successfully refreshed jwt and refresh token");
        return new LoginResponse(jwt, newToken);
    }

    public void logout(String refreshToken) {
        log.debug("Trying to log out User");
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Successfully logged out");
    }

    @Transactional
    public User register(RegisterRequest dto) {
        String email = dto.email().trim().toLowerCase();
        log.debug("Trying to register User with Email {}", email);
        if (dto.role().equals(UserRole.ADMIN))
            throw new NotAllowedException("ADMIN registration is not allowed");
        User user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            if (user.isEnabled())
                throw new DuplicateResourceException("User already exists");
        } else {
            user = userRepo.save(
                    User.builder().email(email).password(encoder.encode(dto.password())).role(dto.role()).build());
            if (dto.role() == UserRole.COACH)
                coachRepo.save(Coach.builder().user(user).build());
            if (dto.role()  == UserRole.CLIENT)
                clientRepo.save(Client.builder().user(user).build());
            log.info("Successfully registered User");
        }
        String jwt = jwtService.createActivationJwt(email);
        emailService.sendActivationMail(email, jwt);
        return user;
    }

    public User activate(String token) {
        log.debug("Trying to activate User");
        DecodedJWT jwt = jwtService.verifyActivation(token);
        User user = userRepo.findByEmailOrThrow(jwt.getClaim("email").asString());
        user.setEnabled(true);
        User savedUser = userRepo.save(user);
        log.info("Successfully activated User {}", savedUser.getId());
        return savedUser;
    }

    public void sendActivationMail(String email) {
        User user = userRepo.findByEmailOrThrow(email);
        if (user.isEnabled())
            throw new NotAllowedException("User already activated");
        String jwt = jwtService.createActivationJwt(email);
        emailService.sendActivationMail(email, jwt);
    }

    public void sendInvitationMail(String clientMail, UUID coachId) {
        userRepo.findByIdOrThrow(coachId);
        String jwt = jwtService.createInvitationJwt(clientMail, coachId);
        emailService.sendInvitationMail(clientMail, jwt);
    }

    @Transactional
    public User acceptInvitation(String token, PasswordRequest request) {
        log.debug("Trying to accept invitation");
        DecodedJWT jwt = jwtService.verifyInvitation(token);
        UUID coachId = UUID.fromString(jwt.getClaim("coachId").asString());
        coachRepo.findByIdOrThrow(coachId);
        User user = register(new RegisterRequest(
                jwt.getClaim("clientEmail").asString(), request.password(), UserRole.CLIENT));
        Client client = clientRepo.findByIdOrThrow(user.getId());
        coachingService.startCoaching(coachId, client.getId());
        log.info("Successfully accepted invitation Coach {} to Client {}", coachId, client.getId());
        return user;
    }

    public void sendForgotMail(String email) {
        User user = userRepo.findByEmailOrThrow(email);
        if (!user.isEnabled())
            throw new DisabledException("User account is disabled");
        String jwt = jwtService.createResetPasswordJwt(email);
        emailService.sendResetPasswordMail(email, jwt);
    }

    public User reset(String token, PasswordRequest request) {
        log.debug("Trying to reset password");
        DecodedJWT jwt = jwtService.verifyResetPassword(token);
        User user = userRepo.findByEmailOrThrow(jwt.getClaim("email").asString());
        user.setPassword(encoder.encode(request.password()));
        User savedUser = userRepo.save(user);
        log.info("Successfully reset password from User {}", user.getId());
        return savedUser;
    }

}
