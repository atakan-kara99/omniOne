package app.omniOne.repo;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    default User findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchResourceException("User not found"));
    }

}
