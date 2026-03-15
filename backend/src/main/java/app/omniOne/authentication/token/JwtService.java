package app.omniOne.authentication.token;

import app.omniOne.authentication.model.UserDetails;
import app.omniOne.exception.custom.JwtExpiredException;
import app.omniOne.exception.custom.JwtInvalidException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${jwt.secret.auth}")
    private String authSecret;
    private Algorithm authAlgorithm;
    private JWTVerifier authVerifier;
    private JWTVerifier resetPasswordVerifier;

    @Value("${jwt.secret.init}")
    private String initSecret;
    private Algorithm initAlgorithm;
    private JWTVerifier activationVerifier;
    private JWTVerifier invitationVerifier;

    @Value("${activation.ttlMins}")
    private int activationTtlMins;
    @Value("${invitation.ttlMins}")
    private int invitationTtlMins;
    @Value("${reset-password.ttlMins}")
    private int resetPasswordTtlMins;
    @Value("${authorization.ttlMins}")
    private int authorizationTtlMins;

    @PostConstruct
    public void init() {
        this.authAlgorithm = Algorithm.HMAC256(authSecret);
        this.authVerifier = JWT.require(authAlgorithm)
                .withIssuer(applicationName)
                .withSubject("authorization").build();
        this.resetPasswordVerifier = JWT.require(authAlgorithm)
                .withIssuer(applicationName)
                .withSubject("reset-password").build();
        this.initAlgorithm = Algorithm.HMAC256(initSecret);
        this.activationVerifier = JWT.require(initAlgorithm)
                .withIssuer(applicationName)
                .withSubject("activation").build();
        this.invitationVerifier = JWT.require(initAlgorithm)
                .withIssuer(applicationName)
                .withSubject("invitation").build();
    }

    public String createAuthJwt(UserDetails user) {
        Map<String, String> claims = Map.of("id", user.getId().toString(), "role", user.getRole());
        return createTemplateJwt("authorization", claims, authorizationTtlMins, authAlgorithm);
    }

    public String createResetPasswordJwt(String email) {
        Map<String, String> claims = Map.of("email", email);
        return createTemplateJwt("reset-password", claims, resetPasswordTtlMins, authAlgorithm);
    }

    public String createActivationJwt(String email) {
        Map<String, String> claims = Map.of("email", email);
        return createTemplateJwt("activation", claims, activationTtlMins, initAlgorithm);
    }

    public String createInvitationJwt(String clientEmail, UUID coachId) {
        Map<String, String> claims = Map.of(
                "clientEmail", clientEmail,
                "coachId", coachId.toString());
        return createTemplateJwt("invitation", claims, invitationTtlMins, initAlgorithm);
    }

    private String createTemplateJwt(String subject, Map<String, String> claims, long minutes, Algorithm algorithm) {
        Builder jwtBuilder = JWT.create()
                .withIssuer(applicationName)
                .withSubject(subject)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES)));
        claims.forEach(jwtBuilder::withClaim);
        return jwtBuilder.sign(algorithm);
    }

    public DecodedJWT verifyAuth(String jwt) {
        return verify(jwt, authVerifier);
    }

    public DecodedJWT verifyResetPassword(String jwt) {
        return verify(jwt, resetPasswordVerifier);
    }

    public DecodedJWT verifyActivation(String jwt) {
        return verify(jwt, activationVerifier);
    }

    public DecodedJWT verifyInvitation(String jwt) {
        return verify(jwt, invitationVerifier);
    }

    private DecodedJWT verify(String jwt, JWTVerifier verifier) {
        try {
            return verifier.verify(jwt);
        } catch (TokenExpiredException ex) {
            throw new JwtExpiredException("Token has expired");
        } catch (JWTVerificationException ex) {
            throw new JwtInvalidException("Token is invalid");
        }
    }

}
