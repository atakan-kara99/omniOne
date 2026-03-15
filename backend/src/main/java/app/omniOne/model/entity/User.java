package app.omniOne.model.entity;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "user_")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean deleted;

    @Column(columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime deletedAt;

    public UserProfile getProfileOrThrow() {
        if (profile == null)
            throw new ResourceNotFoundException("UserProfile not found");
        return profile;
    }

}
