package app.omniOne.authentication;

import app.omniOne.authentication.model.UserDetails;
import app.omniOne.authentication.model.dto.*;
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

    public LoginResponse login(LoginRequest request, UUID deviceId) {
        log.debug("Trying to log in User {}", request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.createAuthJwt(userDetails);
        String token = refreshTokenService.generateToken();
        refreshTokenService.saveRefreshToken(token, userDetails.getUser(), deviceId);
        log.info("Successfully logged in");
        return new LoginResponse(jwt, token);
    }

    public LoginResponse refreshTokens(String rawToken, UUID deviceId) {
        log.debug("Trying to refresh jwt for User");
        RefreshToken refreshToken = refreshTokenService.getRefreshToken(rawToken, deviceId);
        UserDetails userDetails = new UserDetails(refreshToken.getUser());
        if (!userDetails.isEnabled())
            throw new DisabledException("User account is disabled/deleted");
        String jwt = jwtService.createAuthJwt(userDetails);
        String newToken = refreshTokenService.rotateRefreshToken(rawToken, deviceId);
        log.info("Successfully refreshed jwt and refresh token");
        return new LoginResponse(jwt, newToken);
    }

    public void logout(String refreshToken) {
        log.debug("Trying to log out User");
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Successfully logged out");
    }

    @Transactional
    public User registerCoach(RegisterRequest dto) {
        String email = normalize(dto.email());
        log.debug("Trying to register Coach with Email {}", email);
        User user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            if (user.isEnabled()) {
                log.info("Registration rejected: user already exists (email={})", email);
                throw new DuplicateResourceException("User already exists");
            }
            if (user.getRole() != UserRole.COACH) {
                log.info("Registration rejected: email reserved for other role (email={}, role={})",
                        email, user.getRole());
                throw new NotAllowedException("Email already reserved");
            }
        } else {
            user = userRepo.save(User.builder()
                    .email(email)
                    .password(encoder.encode(dto.password()))
                    .role(UserRole.COACH)
                    .enabled(false).build());
            coachRepo.save(Coach.builder().user(user).build());
            log.info("Successfully registered Coach");
        }
        sendActivationMail(email);
        return user;
    }

    public User activate(String token) {
        log.debug("Trying to activate User");
        DecodedJWT jwt = jwtService.verifyActivation(token);
        String email = normalize(jwt.getClaim("email").asString());
        User user = userRepo.findByEmailOrThrow(email);
        if (user.isEnabled()) {
            log.info("Activation rejected: already activated (email={})", email);
            throw new NotAllowedException("User already activated");
        }
        user.setEnabled(true);
        User savedUser = userRepo.save(user);
        log.info("Successfully activated User {}", savedUser.getId());
        return savedUser;
    }

    public void sendActivationMail(String email) {
        email = normalize(email);
        User user = userRepo.findByEmailOrThrow(email);
        if (user.isEnabled()) {
            log.info("Activation mail rejected: already activated (email={})", email);
            throw new NotAllowedException("User already activated");
        }
        String jwt = jwtService.createActivationJwt(email);
        emailService.sendActivationMail(email, jwt);
    }

    public void sendInvitationMail(String clientMail, UUID coachId) {
        clientMail = normalize(clientMail);
        coachRepo.findByIdOrThrow(coachId);
        User user = userRepo.findByEmail(clientMail).orElse(null);
        if (user != null) {
            if (user.getRole() != UserRole.CLIENT) {
                log.info("Invitation rejected: existing user not a client (email={}, role={})",
                        clientMail, user.getRole());
                throw new NotAllowedException("Existing user is not a client");
            }
            Client client = clientRepo.findByIdOrThrow(user.getId());
            if (client.getCoach() != null) {
                log.info("Invitation rejected: client already has coach (clientId={}, coachId={})",
                        user.getId(), client.getCoach().getId());
                throw new NotAllowedException("Client already has a coach");
            }
        }
        String jwt = jwtService.createInvitationJwt(clientMail, coachId);
        emailService.sendInvitationMail(clientMail, jwt);
    }

    public InvitationResponse validateInvitation(String token) {
        log.debug("Trying to validate invitation");
        DecodedJWT jwt = jwtService.verifyInvitation(token);
        UUID coachId = UUID.fromString(jwt.getClaim("coachId").asString());
        coachRepo.findByIdOrThrow(coachId);
        String clientMail = normalize(jwt.getClaim("clientEmail").asString());
        User user = userRepo.findByEmail(clientMail).orElse(null);
        if (user == null)
            return new InvitationResponse(clientMail, true);
        if (user.getRole() != UserRole.CLIENT) {
            log.info("Invitation validation rejected: existing user not a client (email={}, role={})",
                    clientMail, user.getRole());
            throw new NotAllowedException("Existing user is not a client");
        }
        Client client = clientRepo.findByIdOrThrow(user.getId());
        if (client.getCoach() != null) {
            log.info("Invitation validation rejected: client already has coach (clientId={}, coachId={})",
                    user.getId(), client.getCoach().getId());
            throw new NotAllowedException("Client already has a coach");
        }
        return new InvitationResponse(clientMail, false);
    }

    @Transactional
    public User acceptInvitation(String token, PasswordRequest request) {
        log.debug("Trying to accept invitation");
        DecodedJWT jwt = jwtService.verifyInvitation(token);
        UUID coachId = UUID.fromString(jwt.getClaim("coachId").asString());
        coachRepo.findByIdOrThrow(coachId);
        String clientMail = normalize(jwt.getClaim("clientEmail").asString());
        User user = userRepo.findByEmail(clientMail).orElse(null);
        Client client;
        if (user != null) {
            if (user.getRole() != UserRole.CLIENT) {
                log.info("Invitation acceptance rejected: existing user not a client (email={}, role={})",
                        clientMail, user.getRole());
                throw new NotAllowedException("Existing user is not a client");
            }
            client = clientRepo.findByIdOrThrow(user.getId());
            if (client.getCoach() != null) {
                log.info("Invitation acceptance rejected: client already has coach (clientId={}, coachId={})",
                        user.getId(), client.getCoach().getId());
                throw new NotAllowedException("Client already has a coach");
            }
        } else {
            if (request == null || request.password() == null || request.password().isBlank()) {
                log.info("Invitation acceptance rejected: missing password (email={})", clientMail);
                throw new NotAllowedException("Password is required for a new client");
            }
            user = userRepo.save(User.builder()
                    .email(clientMail)
                    .password(encoder.encode(request.password()))
                    .role(UserRole.CLIENT)
                    .enabled(true).build());
            client = clientRepo.save(Client.builder().user(user).build());
        }
        coachingService.startCoaching(coachId, client.getId());
        log.info("Successfully accepted invitation Coach {} to Client {}", coachId, client.getId());
        return user;
    }

    public void sendForgotMail(String email) {
        email = normalize(email);
        User user = userRepo.findByEmailOrThrow(email);
        if (!user.isEnabled() || user.isDeleted())
            throw new DisabledException("User account is disabled/deleted");
        String jwt = jwtService.createResetPasswordJwt(email);
        emailService.sendResetPasswordMail(email, jwt);
    }

    public User reset(String token, PasswordRequest request) {
        log.debug("Trying to reset password");
        DecodedJWT jwt = jwtService.verifyResetPassword(token);
        String email = normalize(jwt.getClaim("email").asString());
        User user = userRepo.findByEmailOrThrow(email);
        if (!user.isEnabled() || user.isDeleted())
            throw new DisabledException("User account is disabled/deleted");
        user.setPassword(encoder.encode(request.password()));
        User savedUser = userRepo.save(user);
        log.info("Successfully reset password from User {}", user.getId());
        return savedUser;
    }

    private String normalize(String string) {
        return string.trim().toLowerCase();
    }

}
