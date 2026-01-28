package app.omniOne.repository;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileRepo extends JpaRepository<UserProfile, UUID> {

    default UserProfile findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchResourceException("UserProfile not found"));
    }

}
