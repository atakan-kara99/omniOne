package app.omniOne.authentication;

import app.omniOne.authentication.model.*;
import app.omniOne.authentication.token.JwtDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@Tag(name = "Auth")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${refresh.token.ttl-days}")
    private int refreshTtlDays;

    private final AuthMapper authMapper;
    private final AuthService authService;

    private ResponseCookie buildRefreshCookie(String refreshToken, Duration duration) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(duration).build();
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/account/login")
    public ResponseEntity<JwtDto> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        ResponseCookie refreshCookie =
                buildRefreshCookie(loginResponse.refreshToken(), Duration.ofDays(refreshTtlDays));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new JwtDto(loginResponse.jwt()));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<JwtDto> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        LoginResponse loginResponse = authService.refreshTokens(refreshToken);
        ResponseCookie refreshCookie =
                buildRefreshCookie(loginResponse.refreshToken(), Duration.ofDays(refreshTtlDays));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new JwtDto(loginResponse.jwt()));
    }

    @PostMapping("/account/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank())
            authService.logout(refreshToken);
        ResponseCookie deleteCookie =
                buildRefreshCookie("", Duration.ZERO);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).build();
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
