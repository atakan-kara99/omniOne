package app.omniOne.auth;

import app.omniOne.auth.jwt.JwtResponse;
import app.omniOne.auth.jwt.JwtService;
import app.omniOne.exception.DuplicateResourceException;
import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.Coach;
import app.omniOne.repo.ClientRepo;
import app.omniOne.repo.CoachRepo;
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
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public boolean isOwner(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) auth.getPrincipal();
        return user.getRole().equals(UserRole.ADMIN) || user.getId().equals(id);
    }

    @Transactional
    public JwtResponse register(UserRegisterDto dto) {
        String email = dto.email().trim().toLowerCase();
        if (dto.role().equals(UserRole.ADMIN))
            throw new NotAllowedException("ADMIN registration is not allowed");
        if (userRepo.existsByEmail(email))
            throw new DuplicateResourceException("User already exists with email: %s".formatted(email));
        User user = userRepo.save(new User(email, encoder.encode(dto.password()), dto.role()));
        if (dto.role().equals(UserRole.COACH))
            coachRepo.save(new Coach(user.getId()));
        if (dto.role().equals(UserRole.CLIENT))
            clientRepo.save(new Client(user.getId()));
        return new JwtResponse(jwtService.createActivationJwt(user), userMapper.map(user));
    }

    public User activate(String token) {
        DecodedJWT jwt = jwtService.verifyActivation(token);
        User user = userRepo.findById(UUID.fromString(jwt.getSubject()))
                .orElseThrow(() -> new NoSuchResourceException("User not found"));
        user.setEnabled(true);
        return userRepo.save(user);
    }

}
