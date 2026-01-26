package app.omniOne.authentication.token;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.exception.RefreshTokenInvalidException;
import app.omniOne.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final String HMAC_ALGO = "HmacSHA256";

    private final int ttlDays;
    private final SecretKeySpec key;
    private final ThreadLocal<Mac> macThreadLocal;
    private final RefreshTokenRepo refreshTokenRepo;
    private final SecureRandom RNG = new SecureRandom();

    public RefreshTokenService(@Value("${refresh.token.secret}") String secret,
                               @Value("${refresh.token.ttl-days}") int ttlDays,
                               RefreshTokenRepo refreshTokenRepo) {
        this.ttlDays = ttlDays;
        this.refreshTokenRepo = refreshTokenRepo;
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
        this.macThreadLocal = ThreadLocal.withInitial(() -> initMac(this.key));
        log.info("RefreshTokenService initialized (ttlDays={}, algo={})", ttlDays, HMAC_ALGO);
    }

    public String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RNG.nextBytes(bytes);
        log.debug("Generated new refresh token (bytes={}, urlSafeBase64NoPadding=true)", TOKEN_BYTES);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void saveRefreshToken(String rawToken, User user) {
        String tokenHash = hash(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(ttlDays);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .lastUsedAt(null).build();
        refreshTokenRepo.save(refreshToken);
        log.info("Saved refresh token (userId={}, tokenHashPrefix={}, expiresAt={})",
                user.getId(), hashPrefix(tokenHash), expiresAt);
    }

    public void revokeRefreshToken(String rawToken) {
        RefreshToken refreshToken = getRefreshToken(rawToken);
        UUID userId = refreshToken.getUser().getId();
        String tokenHash = refreshToken.getTokenHash();
        if (refreshToken.isRevoked()) {
            log.warn("Attempt to revoke already revoked refresh token (userId={}, tokenHashPrefix={})",
                    userId, hashPrefix(tokenHash));
            throw new RefreshTokenInvalidException("RefreshToken already revoked");
        }
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepo.save(refreshToken);
        log.info("Revoked refresh token (userId={}, tokenHashPrefix={})",
                userId, hashPrefix(tokenHash));
    }

    public String rotateRefreshToken(String rawToken) {
        String newToken = generateToken();
        RefreshToken refreshToken = getRefreshToken(rawToken);
        refreshToken.setTokenHash(hash(newToken));
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepo.save(refreshToken);
        log.info("Rotated refresh token (userId={}, oldTokenHashPrefix={}, newTokenHashPrefix={})",
                refreshToken.getUser().getId(), hashPrefix(refreshToken.getTokenHash()), hashPrefix(hash(newToken)));
        return newToken;
    }

    public RefreshToken getRefreshToken(String rawToken) {
        String hashToken = hash(rawToken);
        try {
            RefreshToken refreshToken = refreshTokenRepo.findByTokenHashOrThrow(hashToken);
            log.debug("Loaded refresh token (userId={}, tokenHashPrefix={}, revoked={}, expiresAt={})",
                    refreshToken.getUser().getId(), hashPrefix(refreshToken.getTokenHash()),
                    refreshToken.isRevoked(), refreshToken.getExpiresAt());
            return refreshToken;
        } catch (NoSuchResourceException ex) {
            log.warn("Invalid refresh token presented (tokenHashPrefix={})", hashPrefix(hashToken));
            throw new RefreshTokenInvalidException(ex.getMessage());
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 0 1 * *")
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedRows = refreshTokenRepo.deleteAllExpired(now);
        log.info("Deleted expired refresh tokens (deletedRows={}, now={})", deletedRows, now);
    }

    public String hash(String rawToken) {
        try {
            Mac mac = macThreadLocal.get();
            mac.reset();
            byte[] out = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception ex) {
            log.error("Failed to hash refresh token (algo={})", HMAC_ALGO, ex);
            throw new IllegalStateException("Failed to hash refresh token", ex);
        }
    }

    private static Mac initMac(SecretKeySpec key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(key);
            return mac;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to initialize HMAC", ex);
        }
    }

    private static String hashPrefix(String fullHash) {
        if (fullHash == null)
            return "null";
        return fullHash.length() <= 12 ? fullHash : fullHash.substring(0, 12);
    }

}
