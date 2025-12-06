package app.omniOne.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto register(@RequestBody @Valid UserRegisterDto dto) {
        return userMapper.map(authService.register(dto));
    }

    @GetMapping("/send-again")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto sendAgain(@RequestParam @Email @NotBlank String email) {
        return userMapper.map(authService.sendActivation(email));
    }

    @GetMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto activate(@RequestParam @NotBlank String token) {
        return userMapper.map(authService.activate(token));
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.OK)
    public void invite(@RequestParam @Email @NotBlank String email, Authentication auth) {
        UserDetails user = (UserDetails) auth.getPrincipal();
        authService.sendInvitation(email, user.getId());
    }

    @GetMapping("/accept")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto accept(@RequestParam @NotBlank String token, @RequestBody @Valid UserRegisterDto dto) {
        User user = authService.register(dto);
        authService.acceptInvitation(token, user.getId());
        return userMapper.map(user);
    }

}
