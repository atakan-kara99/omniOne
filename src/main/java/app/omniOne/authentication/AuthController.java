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
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Auth")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String DEVICE_ID_HEADER = "X-Device-Id";
    private static final String REFRESH_TOKEN_HEADER = "refresh_token";

    @Value("${refresh.token.ttl-days}")
    private int refreshTtlDays;

    private final AuthMapper authMapper;
    private final AuthService authService;

    private ResponseCookie buildRefreshCookie(String refreshToken, Duration duration) {
        return ResponseCookie.from(REFRESH_TOKEN_HEADER, refreshToken)
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
    public ResponseEntity<JwtDto> login(@RequestBody @Valid LoginRequest request,
                                        @RequestHeader(name = DEVICE_ID_HEADER, required = false) UUID deviceId) {
        UUID resolvedDeviceId = deviceId != null ? deviceId : UUID.randomUUID();
        LoginResponse loginResponse = authService.login(request, resolvedDeviceId);
        ResponseCookie refreshCookie =
                buildRefreshCookie(loginResponse.refreshToken(), Duration.ofDays(refreshTtlDays));
        return ResponseEntity.ok()
                .header(DEVICE_ID_HEADER, resolvedDeviceId.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new JwtDto(loginResponse.jwt()));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<JwtDto> refresh(@CookieValue(name = REFRESH_TOKEN_HEADER, required = false) String refreshToken,
                                          @RequestHeader(name = DEVICE_ID_HEADER) UUID deviceId) {
        LoginResponse loginResponse = authService.refreshTokens(refreshToken, deviceId);
        ResponseCookie refreshCookie =
                buildRefreshCookie(loginResponse.refreshToken(), Duration.ofDays(refreshTtlDays));
        return ResponseEntity.ok()
                .header(DEVICE_ID_HEADER, deviceId.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new JwtDto(loginResponse.jwt()));
    }

    @PostMapping("/account/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_TOKEN_HEADER, required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank())
            authService.logout(refreshToken);
        ResponseCookie deleteCookie =
                buildRefreshCookie("", Duration.ZERO);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).build();
    }

    @PostMapping("/account/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authMapper.map(authService.registerCoach(request));
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
    public AuthResponse accept(@RequestParam @NotBlank String token,
                               @RequestBody(required = false) @Valid PasswordRequest request) {
        return authMapper.map(authService.acceptInvitation(token, request));
    }

    @GetMapping("/invitation/validate")
    @ResponseStatus(HttpStatus.OK)
    public InvitationResponse validateInvitation(@RequestParam @NotBlank String token) {
        return authService.validateInvitation(token);
    }

}
