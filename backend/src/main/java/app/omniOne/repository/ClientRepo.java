package app.omniOne.repository;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.dto.ClientResponse;
import app.omniOne.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRepo extends JpaRepository<Client, UUID> {

    default Client findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    List<Client> findAllByCoachId(UUID coachId);

    @Query("""
    SELECT new app.omniOne.model.dto.ClientResponse(c.id, up.firstName, up.lastName)
    FROM Client c
    JOIN UserProfile up ON up.id = c.id
    WHERE c.coach.id = :coachId
    """)
    List<ClientResponse> findClientsByCoachId(UUID coachId);

}
