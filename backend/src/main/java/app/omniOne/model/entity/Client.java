package app.omniOne.model.entity;

import app.omniOne.model.enums.ClientStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client")
public class Client {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    private Coach coach;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @OneToMany(mappedBy = "client", cascade = CascadeType.REMOVE)
    private List<NutriPlan> nutriPlans;

    public Client(UUID id) {
        this.id = id;
        this.status = ClientStatus.PENDING;
    }

    public Client(UUID id, Coach coach) {
        this.id = id;
        this.status = ClientStatus.PENDING;
        this.coach = coach;
    }

}
