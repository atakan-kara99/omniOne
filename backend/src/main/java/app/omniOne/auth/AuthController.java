package app.omniOne.auth;

import app.omniOne.auth.jwt.JwtResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public JwtResponse register(@RequestBody @Valid UserRegisterDto dto) {
        return authService.register(dto);
    }

    @GetMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto activate(@RequestParam String token) {
        return userMapper.map(authService.activate(token));
    }

}
