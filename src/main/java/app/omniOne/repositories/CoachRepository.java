package app.omniOne.repositories;

import app.omniOne.models.entities.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {

    boolean existsByEmail(String email);

}
