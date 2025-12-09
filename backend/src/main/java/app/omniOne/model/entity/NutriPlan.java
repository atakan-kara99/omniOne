package app.omniOne.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "nutrition_plan")
public class NutriPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private Double carbs;

    @Column(nullable = false)
    private Double proteins;

    @Column(nullable = false)
    private Double fats;

    private Double calories;

    private Double water;

    private Double salt;

    private Double fiber;

    @PrePersist
    @PreUpdate
    private void computeCalories() {
        this.calories = carbs * 4.1 + proteins * 4.1 + fats * 9.3;
    }

}
