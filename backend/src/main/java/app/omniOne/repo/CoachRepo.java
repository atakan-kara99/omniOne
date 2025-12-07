package app.omniOne.repo;

import app.omniOne.exception.NoSuchResourceException;
import app.omniOne.model.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoachRepo extends JpaRepository<Coach, UUID> {

    default Coach findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchResourceException("Coach not found"));
    }

}
