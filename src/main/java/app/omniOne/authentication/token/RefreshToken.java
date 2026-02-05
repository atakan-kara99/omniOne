package app.omniOne.authentication.token;

import app.omniOne.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "refresh_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_token_hash", columnNames = "token_hash"),
                @UniqueConstraint(name = "uk_refresh_token_user_device", columnNames = {"user_id", "device_id"})
        })
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "token_hash", nullable = false, length = 64)
    String tokenHash;

    @Column(name = "device_id", nullable = false)
    UUID deviceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    LocalDateTime lastUsedAt;

    @Column(nullable = false)
    LocalDateTime expiresAt;

    LocalDateTime revokedAt;

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

}
