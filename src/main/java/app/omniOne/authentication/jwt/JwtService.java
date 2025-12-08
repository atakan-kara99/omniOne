package app.omniOne.authentication.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${jwt.secret.auth}")
    private String authSecret;
    private Algorithm authAlgorithm;
    private JWTVerifier authVerifier;

    @Value("${jwt.secret.init}")
    private String initSecret;
    private Algorithm initAlgorithm;
    private JWTVerifier initVerifier;

    @PostConstruct
    public void init() {
        this.authAlgorithm = Algorithm.HMAC256(authSecret);
        this.authVerifier = JWT.require(authAlgorithm).withIssuer(applicationName).build();
        this.initAlgorithm = Algorithm.HMAC256(initSecret);
        this.initVerifier = JWT.require(initAlgorithm).withIssuer(applicationName).build();
    }

    public String createAuthJwt(String email) {
        Map<String, String> claims = Map.of("email", email);
        return createTemplateJwt("authorization", claims, 60, authAlgorithm);
    }

    public String createActivationJwt(String email) {
        Map<String, String> claims = Map.of("email", email);
        return createTemplateJwt("activation", claims, 60*24, initAlgorithm);
    }

    public String createResetPasswordJwt(String email) {
        Map<String, String> claims = Map.of("email", email);
        return createTemplateJwt("reset-password", claims, 60, initAlgorithm);
    }

    public String createInvitationJwt(String clientEmail, UUID coachId) {
        Map<String, String> claims = Map.of(
                "clientEmail", clientEmail,
                "coachId", coachId.toString());
        return createTemplateJwt("invitation", claims, 60*24, initAlgorithm);
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
        return authVerifier.verify(jwt);
    }

    public DecodedJWT verifyActivation(String jwt) {
        return initVerifier.verify(jwt);
    }

    public DecodedJWT verifyInvitation(String jwt) {
        return initVerifier.verify(jwt);
    }

}
