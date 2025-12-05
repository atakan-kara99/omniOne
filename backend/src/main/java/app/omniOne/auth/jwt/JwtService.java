package app.omniOne.auth.jwt;

import app.omniOne.auth.User;
import app.omniOne.auth.UserLoginDto;
import app.omniOne.auth.UserMapper;
import app.omniOne.auth.UserRepo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${jwt.secret.auth}")
    private String authSecret;
    private Algorithm authAlgorithm;
    private JWTVerifier authVerifier;

    @Value("${jwt.secret.auth}")
    private String activationSecret;
    private Algorithm activationAlgorithm;
    private JWTVerifier activationVerifier;

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    @PostConstruct
    public void init() {
        this.authAlgorithm = Algorithm.HMAC256(authSecret);
        this.authVerifier = JWT.require(authAlgorithm).withIssuer(applicationName).build();
        this.activationAlgorithm = Algorithm.HMAC256(activationSecret);
        this.activationVerifier = JWT.require(activationAlgorithm).withIssuer(applicationName).build();
    }

    public String createAuthJwt(String username) {
        return JWT.create()
                .withIssuer(applicationName)
                .withSubject(username)
                .withClaim("type", "authorization")
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(authAlgorithm);
    }

    public DecodedJWT verifyAuth(String jwt) {
        return authVerifier.verify(jwt);
    }

    public JwtResponse getJwt(UserLoginDto dto) {
        String email = dto.email().trim().toLowerCase();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!encoder.matches(dto.password(), user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");
        return new JwtResponse(createAuthJwt(email), userMapper.map(user));
    }

    public String createActivationJwt(User user) {
        return JWT.create()
                .withIssuer(applicationName)
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("type", "activation")
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .sign(activationAlgorithm);
    }

    public DecodedJWT verifyActivation(String jwt) {
        return activationVerifier.verify(jwt);
    }

}
