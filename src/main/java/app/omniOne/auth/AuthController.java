package app.omniOne.auth;

import app.omniOne.auth.model.AuthDto;
import app.omniOne.auth.model.AuthMapper;
import app.omniOne.auth.model.PasswordRequest;
import app.omniOne.auth.model.RegisterRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthMapper authMapper;
    private final AuthService authService;

    @PostMapping("/account/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDto register(@RequestBody @Valid RegisterRequest dto) {
        return authMapper.map(authService.register(dto));
    }

    @GetMapping("/account/activate")
    @ResponseStatus(HttpStatus.OK)
    public AuthDto activate(@RequestParam @NotBlank String token) {
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
    public AuthDto reset(@RequestParam @NotBlank String token, @RequestBody @Valid PasswordRequest request) {
        return authMapper.map(authService.reset(token, request));
    }

    @GetMapping("/invitation/accept")
    @ResponseStatus(HttpStatus.OK)
    public AuthDto accept(@RequestParam @NotBlank String token, @RequestBody @Valid PasswordRequest request) {
        return authMapper.map(authService.acceptInvitation(token, request));
    }

}
