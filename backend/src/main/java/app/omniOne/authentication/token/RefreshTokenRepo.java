package app.omniOne.authentication.token;

import app.omniOne.exception.NoSuchResourceException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    default RefreshToken findByTokenHashOrThrow(String tokenHash) {
        return findByTokenHash(tokenHash)
                .orElseThrow(() -> new NoSuchResourceException("RefreshToken not found"));
    }

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteAllExpired(LocalDateTime now);

}
