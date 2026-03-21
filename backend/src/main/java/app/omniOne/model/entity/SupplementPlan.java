package app.omniOne.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "supplement_plan")
public class SupplementPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "supplementPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SupplementEntry> supplementEntries = new ArrayList<>();

}
