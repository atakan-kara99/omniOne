package app.omniOne.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String applicationName;

    public JwtService(@Value("${spring.application.name}") String applicationName,
                      @Value("${spring.jwt.secret}") String secret) {
        this.applicationName = applicationName;
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(applicationName).build();
    }

    public String createJwt(String username) {
        return JWT.create()
                .withIssuer(applicationName)
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date((long) (System.currentTimeMillis() + 3600_000 * 1.0))) // 1 hour
                .sign(algorithm);
    }

    public DecodedJWT verify(String jwt) {
        return verifier.verify(jwt);
    }

}
