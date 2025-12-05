package app.omniOne.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coach")
public class Coach {

    @Id
    private UUID id;

    @OneToMany(mappedBy = "coach")
    private List<Client> clients;

    public Coach(UUID id) {
        this.id = id;
    }

}
