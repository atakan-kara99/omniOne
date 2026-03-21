package app.omniOne.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "supplement_entry")
public class SupplementEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_plan_id", nullable = false)
    private SupplementPlan supplementPlan;

    @Column(nullable = false)
    private String supplementName;

    private String brand;

    @Column(nullable = false)
    private String dosage;

    private String timing;

    @Column(columnDefinition = "TEXT")
    private String notes;

}
