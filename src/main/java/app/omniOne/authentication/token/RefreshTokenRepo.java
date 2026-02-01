package app.omniOne.authentication.token;

import app.omniOne.exception.NoSuchResourceException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {

    List<RefreshToken> findAllByUserId(UUID userId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByTokenHashAndDeviceId(String tokenHash, UUID deviceId);

    Optional<RefreshToken> findByUserIdAndDeviceIdAndRevokedAtIsNull(UUID userId, UUID deviceId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteAllExpired(LocalDateTime now);

    default RefreshToken findByUserId(String tokenHash) {
        return findByTokenHash(tokenHash)
                .orElseThrow(() -> new NoSuchResourceException("RefreshToken not found"));
    }

    default RefreshToken findByTokenHashAndDeviceIdOrThrow(String tokenHash, UUID deviceId) {
        return findByTokenHashAndDeviceId(tokenHash, deviceId)
                .orElseThrow(() -> new NoSuchResourceException("RefreshToken not found"));
    }

    default RefreshToken findByTokenHashOrThrow(String tokenHash) {
        return findByTokenHash(tokenHash)
                .orElseThrow(() -> new NoSuchResourceException("RefreshToken not found"));
    }

}
