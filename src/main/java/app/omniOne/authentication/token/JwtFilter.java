package app.omniOne.authentication.token;

import app.omniOne.exception.ErrorCode;
import app.omniOne.exception.ProblemDetailFactory;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ProblemDetailFactory problemDetailFactory;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String jwt = authHeader.substring(7);
                DecodedJWT decodedJwt = jwtService.verifyAuth(jwt);
                String id = decodedJwt.getClaim("id").asString();
                String role = decodedJwt.getClaim("role").asString();
                if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    id, null, Collections.singleton(new SimpleGrantedAuthority(role)));
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    MDC.put("userId", id);
                    MDC.put("role", role.substring(5));
                    String deviceId = request.getHeader("X-Device-Id");
                    if (deviceId != null && !deviceId.isBlank()) {
                        MDC.put("deviceId", deviceId);
                    }
                }
            } catch (Exception ex) {
                log.warn("Invalid JWT or unauthorized access");
                problemDetailFactory.write(
                        request,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.AUTH_INVALID_TOKEN,
                        ErrorCode.AUTH_INVALID_TOKEN.title(),
                        "Invalid or expired token",
                        Map.of());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

}
