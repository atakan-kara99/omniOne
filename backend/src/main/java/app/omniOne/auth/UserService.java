package app.omniOne.auth;

import app.omniOne.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    public User register(UserRegisterDto dto) {
        String email = dto.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email))
            throw new DuplicateResourceException("User already exists with email: %s".formatted(email));
        return userRepo.save(new User(email, encoder.encode(dto.password()), dto.role()));
    }

    public String login(UserLoginDto dto) {
        String email = dto.email().trim().toLowerCase();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!encoder.matches(dto.password(), user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");
        return jwtService.createJwt(email);
    }

}
