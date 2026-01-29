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

    public RefreshTokenService(@Value("${refresh-token.secret}") String secret,
                               @Value("${refresh-token.ttl-days}") int ttlDays,
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

    public void saveRefreshToken(String rawToken, User user, UUID deviceId) {
        if (deviceId == null) {
            log.warn("Missing deviceId when saving refresh token (userId={})", user.getId());
            throw new RefreshTokenInvalidException("X-Device-Id header is required");
        }
        String tokenHash = hash(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(ttlDays);
        RefreshToken refreshToken = refreshTokenRepo.findByUserIdAndDeviceIdAndRevokedAtIsNull(user.getId(), deviceId)
                .orElse(null);
        if (refreshToken == null) {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .deviceId(deviceId)
                    .tokenHash(tokenHash)
                    .expiresAt(expiresAt)
                    .lastUsedAt(null)
                    .revokedAt(null).build();
            refreshTokenRepo.save(refreshToken);
            log.info("Inserted refresh token (userId={}, deviceId={}, tokenHashPrefix={}, expiresAt={})",
                    user.getId(), deviceId, hashPrefix(tokenHash), expiresAt);
            return;
        }
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setLastUsedAt(null);
        refreshTokenRepo.save(refreshToken);
        log.info("Updated refresh token (userId={}, deviceId={}, tokenHashPrefix={}, expiresAt={})",
                user.getId(), deviceId, hashPrefix(tokenHash), expiresAt);
    }

    public void revokeRefreshToken(String rawToken) {
        RefreshToken refreshToken = getRefreshToken(rawToken);
        UUID userId = refreshToken.getUser().getId();
        String tokenHash = refreshToken.getTokenHash();
        if (refreshToken.isRevoked()) {
            log.info("Attempt to revoke already revoked refresh token (userId={}, tokenHashPrefix={})",
                    userId, hashPrefix(tokenHash));
            throw new RefreshTokenInvalidException("RefreshToken already revoked");
        }
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepo.save(refreshToken);
        log.info("Revoked refresh token (userId={}, tokenHashPrefix={})",
                userId, hashPrefix(tokenHash));
    }

    public String rotateRefreshToken(String rawToken, UUID deviceId) {
        String newToken = generateToken();
        RefreshToken refreshToken = getRefreshToken(rawToken, deviceId);
        String oldHash = refreshToken.getTokenHash();
        refreshToken.setTokenHash(hash(newToken));
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepo.save(refreshToken);
        log.info("Rotated refresh token (userId={}, oldTokenHashPrefix={}, newTokenHashPrefix={})",
                refreshToken.getUser().getId(), hashPrefix(oldHash), hashPrefix(hash(newToken)));
        return newToken;
    }

    public RefreshToken getRefreshToken(String rawToken) {
        return getRefreshToken(rawToken, null, false);
    }

    public RefreshToken getRefreshToken(String rawToken, UUID deviceId) {
        return getRefreshToken(rawToken, deviceId, true);
    }

    private RefreshToken getRefreshToken(String rawToken, UUID deviceId, boolean requireDeviceId) {
        String tokenHash = requireValidToken(rawToken, requireDeviceId, deviceId);
        RefreshToken refreshToken = loadRefreshToken(tokenHash, deviceId);
        log.debug("Loaded refresh token (userId={}, deviceId={}, tokenHashPrefix={}, revoked={}, expiresAt={})",
                refreshToken.getUser().getId(),
                refreshToken.getDeviceId(),
                hashPrefix(refreshToken.getTokenHash()),
                refreshToken.isRevoked(),
                refreshToken.getExpiresAt());
        validateRefreshToken(refreshToken, requireDeviceId);
        return refreshToken;
    }

    private String requireValidToken(String rawToken, boolean requireDeviceId, UUID deviceId) {
        if (rawToken == null || rawToken.isBlank()) {
            log.info("Invalid refresh token presented (null or blank, deviceRequired={})", requireDeviceId);
            throw new RefreshTokenInvalidException("Cookie 'refresh_token' is not present");
        }
        if (requireDeviceId && deviceId == null) {
            log.info("Missing deviceId for refresh token validation");
            throw new RefreshTokenInvalidException("X-Device-Id header is required");
        }
        return hash(rawToken);
    }

    private RefreshToken loadRefreshToken(String tokenHash, UUID deviceId) {
        try {
            if (deviceId == null) {
                return refreshTokenRepo.findByTokenHashOrThrow(tokenHash);
            }
            return refreshTokenRepo.findByTokenHashAndDeviceIdOrThrow(tokenHash, deviceId);
        } catch (NoSuchResourceException ex) {
            if (deviceId == null) {
                log.info("Invalid refresh token presented (tokenHashPrefix={})", hashPrefix(tokenHash));
                throw new RefreshTokenInvalidException("Invalid refresh token");
            }
            log.info("Invalid refresh token presented (tokenHashPrefix={}, deviceId={})",
                    hashPrefix(tokenHash), deviceId);
            throw new RefreshTokenInvalidException("Invalid refresh token or deviceId");
        }
    }

    private void validateRefreshToken(RefreshToken refreshToken, boolean requireDeviceId) {
        if (requireDeviceId && refreshToken.getDeviceId() == null) {
            log.warn("Refresh token missing deviceId (userId={}, tokenHashPrefix={})",
                    refreshToken.getUser().getId(), hashPrefix(refreshToken.getTokenHash()));
            throw new RefreshTokenInvalidException("Refresh token missing deviceId");
        }
        if (refreshToken.isExpired()) {
            log.info("Refresh token expired (userId={}, tokenHashPrefix={}, expiresAt={})",
                    refreshToken.getUser().getId(),
                    hashPrefix(refreshToken.getTokenHash()),
                    refreshToken.getExpiresAt());
            throw new RefreshTokenInvalidException("Refresh token expired");
        }
        if (refreshToken.isRevoked()) {
            log.info("Refresh token revoked (userId={}, tokenHashPrefix={})",
                    refreshToken.getUser().getId(),
                    hashPrefix(refreshToken.getTokenHash()));
            throw new RefreshTokenInvalidException("Refresh token revoked");
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
