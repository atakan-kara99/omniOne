package app.omniOne.authentication;

import app.omniOne.authentication.model.UserDetails;
import app.omniOne.authentication.model.dto.*;
import app.omniOne.authentication.token.JwtService;
import app.omniOne.authentication.token.RefreshToken;
import app.omniOne.authentication.token.RefreshTokenService;
import app.omniOne.chatting.repository.ChatParticipantRepo;
import app.omniOne.email.EmailService;
import app.omniOne.exception.custom.AccountDisabledException;
import app.omniOne.exception.custom.JwtInvalidException;
import app.omniOne.exception.custom.OperationNotAllowedException;
import app.omniOne.exception.custom.ResourceConflictException;
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
        return coachingRepo.existsByCoachIdAndClientId(coachId, clientId);
    }

    public boolean isRelated(UUID userId1, UUID userId2) {
        return coachingRepo.existsByCoachIdAndClientId(userId1, userId2) ||
                coachingRepo.existsByCoachIdAndClientId(userId2, userId1);
    }

    public boolean isChatOf(UUID userId, UUID conversationId) {
        return chatParticipantRepo.existsByConversationIdAndUserId(conversationId, userId);
    }

    public LoginResponse login(LoginRequest request, UUID deviceId) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.createAuthJwt(userDetails);
        String token = refreshTokenService.generateToken();
        refreshTokenService.saveRefreshToken(token, userDetails.getUser(), deviceId);
        log.info("User logged in (userId={})", userDetails.getUser().getId());
        return new LoginResponse(jwt, token);
    }

    public LoginResponse refreshTokens(String rawToken, UUID deviceId) {
        RefreshToken refreshToken = refreshTokenService.getRefreshToken(rawToken, deviceId);
        UserDetails userDetails = new UserDetails(refreshToken.getUser());
        if (!userDetails.isEnabled())
            throw new AccountDisabledException("User account is disabled/deleted");
        String jwt = jwtService.createAuthJwt(userDetails);
        String newToken = refreshTokenService.rotateRefreshToken(rawToken, deviceId);
        log.info("Tokens refreshed (userId={})", refreshToken.getUser().getId());
        return new LoginResponse(jwt, newToken);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("User logged out");
    }

    @Transactional
    public User registerCoach(RegisterRequest dto) {
        String email = normalize(dto.email());
        User user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            if (user.isEnabled()) {
                log.info("Registration rejected: user already exists (role={})", user.getRole());
                throw new ResourceConflictException("User already exists");
            }
            if (user.getRole() != UserRole.COACH) {
                log.info("Registration rejected: email reserved for other role (role={})", user.getRole());
                throw new OperationNotAllowedException("Email already reserved");
            }
        } else {
            user = userRepo.save(User.builder()
                    .email(email)
                    .password(encoder.encode(dto.password()))
                    .role(UserRole.COACH)
                    .enabled(false).build());
            coachRepo.save(Coach.builder().user(user).build());
            log.info("Coach registered (userId={})", user.getId());
        }
        sendActivationMail(email);
        return user;
    }

    public User activate(String token) {
        DecodedJWT jwt = jwtService.verifyActivation(token);
        String email = normalize(requiredClaim(jwt, "email"));
        User user = userRepo.findByEmailOrThrow(email);
        if (user.isEnabled()) {
            log.info("Activation rejected: already activated (userId={})", user.getId());
            throw new OperationNotAllowedException("User already activated");
        }
        user.setEnabled(true);
        User savedUser = userRepo.save(user);
        log.info("User activated (userId={})", savedUser.getId());
        return savedUser;
    }

    public void sendActivationMail(String email) {
        email = normalize(email);
        User user = userRepo.findByEmailOrThrow(email);
        if (user.isEnabled()) {
            log.info("Activation mail rejected: already activated (userId={})", user.getId());
            throw new OperationNotAllowedException("User already activated");
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
                log.info("Invitation rejected: existing user not a client (role={}, coachId={})",
                        user.getRole(), coachId);
                throw new OperationNotAllowedException("Existing user is not a client");
            }
            Client client = clientRepo.findByIdOrThrow(user.getId());
            if (client.getCoach() != null) {
                log.info("Invitation rejected: client already has coach (clientId={}, coachId={})",
                        user.getId(), client.getCoach().getId());
                throw new OperationNotAllowedException("Client already has a coach");
            }
        }
        String jwt = jwtService.createInvitationJwt(clientMail, coachId);
        emailService.sendInvitationMail(clientMail, jwt);
    }

    public InvitationResponse validateInvitation(String token) {
        DecodedJWT jwt = jwtService.verifyInvitation(token);
        UUID coachId = requiredUuidClaim(jwt, "coachId");
        coachRepo.findByIdOrThrow(coachId);
        String clientMail = normalize(requiredClaim(jwt, "clientEmail"));
        User user = userRepo.findByEmail(clientMail).orElse(null);
        if (user == null)
            return new InvitationResponse(clientMail, true);
        if (user.getRole() != UserRole.CLIENT) {
            log.info("Invitation validation rejected: existing user not a client (role={}, coachId={})",
                    user.getRole(), coachId);
            throw new OperationNotAllowedException("Existing user is not a client");
        }
        Client client = clientRepo.findByIdOrThrow(user.getId());
        if (client.getCoach() != null) {
            log.info("Invitation validation rejected: client already has coach (clientId={}, coachId={})",
                    user.getId(), client.getCoach().getId());
            throw new OperationNotAllowedException("Client already has a coach");
        }
        return new InvitationResponse(clientMail, false);
    }

    @Transactional
    public User acceptInvitation(String token, PasswordRequest request) {
        DecodedJWT jwt = jwtService.verifyInvitation(token);
        UUID coachId = requiredUuidClaim(jwt, "coachId");
        coachRepo.findByIdOrThrow(coachId);
        String clientMail = normalize(requiredClaim(jwt, "clientEmail"));
        User user = userRepo.findByEmail(clientMail).orElse(null);
        Client client;
        if (user != null) {
            if (user.getRole() != UserRole.CLIENT) {
                log.info("Invitation acceptance rejected: existing user not a client (role={}, coachId={})",
                        user.getRole(), coachId);
                throw new OperationNotAllowedException("Existing user is not a client");
            }
            client = clientRepo.findByIdOrThrow(user.getId());
            if (client.getCoach() != null) {
                log.info("Invitation acceptance rejected: client already has coach (clientId={}, coachId={})",
                        user.getId(), client.getCoach().getId());
                throw new OperationNotAllowedException("Client already has a coach");
            }
        } else {
            if (request == null || request.password() == null || request.password().isBlank()) {
                log.info("Invitation acceptance rejected: missing password (coachId={})", coachId);
                throw new OperationNotAllowedException("Password is required for a new client");
            }
            user = userRepo.save(User.builder()
                    .email(clientMail)
                    .password(encoder.encode(request.password()))
                    .role(UserRole.CLIENT)
                    .enabled(true).build());
            client = clientRepo.save(Client.builder().user(user).build());
        }
        coachingService.startCoaching(coachId, client.getId());
        log.info("Invitation accepted (coachId={}, clientId={})", coachId, client.getId());
        return user;
    }

    public void sendForgotMail(String email) {
        email = normalize(email);
        User user = userRepo.findByEmailOrThrow(email);
        if (!user.isEnabled() || user.isDeleted())
            throw new AccountDisabledException("User account is disabled/deleted");
        String jwt = jwtService.createResetPasswordJwt(email);
        emailService.sendResetPasswordMail(email, jwt);
    }

    public User reset(String token, PasswordRequest request) {
        DecodedJWT jwt = jwtService.verifyResetPassword(token);
        String email = normalize(requiredClaim(jwt, "email"));
        User user = userRepo.findByEmailOrThrow(email);
        if (!user.isEnabled() || user.isDeleted())
            throw new AccountDisabledException("User account is disabled/deleted");
        user.setPassword(encoder.encode(request.password()));
        User savedUser = userRepo.save(user);
        log.info("Password reset (userId={})", savedUser.getId());
        return savedUser;
    }

    private String normalize(String string) {
        return string.trim().toLowerCase();
    }

    private String requiredClaim(DecodedJWT jwt, String claimName) {
        String value = jwt.getClaim(claimName).asString();
        if (value == null || value.isBlank())
            throw new JwtInvalidException("Token is invalid");
        return value;
    }

    private UUID requiredUuidClaim(DecodedJWT jwt, String claimName) {
        String value = requiredClaim(jwt, claimName);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new JwtInvalidException("Token is invalid");
        }
    }

}
