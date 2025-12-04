package app.omniOne.repo;

import app.omniOne.model.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoachRepo extends JpaRepository<Coach, Long> {

    boolean existsByEmail(String email);

}
