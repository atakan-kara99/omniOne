package app.omniOne.authentication;

import app.omniOne.authentication.jwt.JwtResponse;
import app.omniOne.authentication.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthMapper authMapper;
    private final AuthService authService;

    @PostMapping("/account/login")
    @ResponseStatus(HttpStatus.OK)
    public JwtResponse login(@RequestBody @Valid LoginRequest request) {
        return new JwtResponse(authService.login(request));
    }

    @PostMapping("/account/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest dto) {
        return authMapper.map(authService.register(dto));
    }

    @GetMapping("/account/activate")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse activate(@RequestParam @NotBlank String token) {
        return authMapper.map(authService.activate(token));
    }

    @GetMapping("/account/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resend(@RequestParam @Email @NotBlank String email) {
        authService.sendActivationMail(email);
    }

    @GetMapping("/password/forgot")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgot(@RequestParam @Email @NotBlank String email) {
        authService.sendForgotMail(email);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse reset(@RequestParam @NotBlank String token, @RequestBody @Valid PasswordRequest request) {
        return authMapper.map(authService.reset(token, request));
    }

    @PostMapping("/invitation/accept")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse accept(@RequestParam @NotBlank String token, @RequestBody @Valid PasswordRequest request) {
        return authMapper.map(authService.acceptInvitation(token, request));
    }

}
