package app.omniOne.auth.jwt;

import com.auth0.jwt.JWT;
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
        return JWT.create()
                .withIssuer(applicationName)
                .withSubject("authorization")
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(authAlgorithm);
    }

    public DecodedJWT verifyAuth(String jwt) {
        return authVerifier.verify(jwt);
    }


    public String createActivationJwt(String email) {
        return JWT.create()
                .withIssuer(applicationName)
                .withSubject("activation")
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .sign(initAlgorithm);
    }

    public DecodedJWT verifyActivation(String jwt) {
        return initVerifier.verify(jwt);
    }


    public String createInvitationJwt(String clientEmail, UUID coachId) {
        return JWT.create()
                .withIssuer(applicationName)
                .withSubject("invitation")
                .withClaim("clientEmail", clientEmail)
                .withClaim("coachId", coachId.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .sign(initAlgorithm);
    }

    public DecodedJWT verifyInvitation(String jwt) {
        return initVerifier.verify(jwt);
    }


}
